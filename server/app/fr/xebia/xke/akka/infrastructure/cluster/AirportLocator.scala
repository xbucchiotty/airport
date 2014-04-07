package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.pattern.ask
import language.postfixOps
import concurrent.duration._
import akka.util.Timeout
import akka.cluster.ClusterEvent.MemberUp
import scala.Some
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.UnreachableMember
import fr.xebia.xke.akka.game.{PlayerDown, PlayerUp}
import fr.xebia.xke.akka.airport.AirportCode
import fr.xebia.xke.akka.infrastructure.{SessionInfo, SessionStore}
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator.AskAirportAddressLookup

class AirportLocator(sessionStore: ActorRef, gameStore: ActorRef) extends Actor with ActorLogging {

  var table: Map[AirportCode, AirportProxy] = _

  override def preStart() {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])
    table = Map.empty
  }

  def receive: Receive = {
    case state: CurrentClusterState =>
      state.members
        .filter(_.status == MemberStatus.Up)
        .filter(memberIsPlayer)
        .foreach(register)

    case MemberUp(member) if memberIsPlayer(member) =>
      register(member)

    case UnreachableMember(member) if memberIsPlayer(member) =>
      unregister(member)

    case AskAirportAddressLookup(airportCode) =>
      sender ! table.get(airportCode)
  }

  def register(member: Member) {
    import context.dispatcher
    implicit val timeout = Timeout(1.second)

    member.roles
      .map(AirportCode)
      .filter(_.code.nonEmpty)
      .foreach(airportCode => {
      log.info(s"<$member> comes in for <$airportCode>")

      val (airTrafficControlProxy, groundControlProxy) =
        if (table.isDefinedAt(airportCode)) {

          log.info(s"Redirecting proxy for <$airportCode> to <$member.address>")

          val proxy = table(airportCode)

          proxy.airTrafficControl ! RemoteProxy.Register(airTrafficControlPath)
          proxy.groundControl ! RemoteProxy.Register(groundControlPath)

          (proxy.airTrafficControl, proxy.groundControl)
        } else {

          log.info(s"Start proxy from <${self.path}> to <$member.address>")

          (context.actorOf(RemoteProxy.props(airTrafficControlPath), s"$airportCode-airTrafficControl"),
            context.actorOf(RemoteProxy.props(groundControlPath), s"$airportCode-groundControl"))
        }

      table += (airportCode -> AirportProxy(member.address, airTrafficControlProxy, groundControlProxy))

      val userInfoRequest = ask(sessionStore, SessionStore.AskForAirport(airportCode)).mapTo[Option[SessionInfo]]

      userInfoRequest.onSuccess {
        case Some(userInfo) => gameStore ! PlayerUp(userInfo.sessionId, member.address)
      }
    })

    def groundControlPath: ActorSelection = {
      context.actorSelection(ActorPath.fromString(member.address.toString) / "user" / "airport" / "groundControl")
    }

    def airTrafficControlPath: ActorSelection = {
      context.actorSelection(ActorPath.fromString(member.address.toString) / "user" / "airport" / "airTrafficControl")
    }
  }

  def unregister(member: Member) {
    implicit val timeout = Timeout(1.second)

    log.warning(s"$member moved out")

    member.roles map AirportCode foreach (airportCode => {
      val proxy = table(airportCode)

      if (proxy.address == member.address) {
        proxy.airTrafficControl ! RemoteProxy.Unregister
        proxy.groundControl ! RemoteProxy.Unregister

        import context.dispatcher
        implicit val timeout = Timeout(10 seconds)
        val userInfoRequest = ask(sessionStore, SessionStore.AskForAirport(airportCode)).mapTo[Option[SessionInfo]]

        userInfoRequest.onSuccess {
          case Some(userInfo) => gameStore ! PlayerDown(userInfo.sessionId, member.address)
        }
      }

    })
  }

  def memberIsPlayer(member: Member) = member.roles.nonEmpty
}

object AirportLocator {

  def props(sessionStore: ActorRef, gameStore: ActorRef): Props = Props(classOf[AirportLocator], sessionStore, gameStore)

  case class AskAirportAddressLookup(airportCode: AirportCode)

}

case class AirportProxy(address: Address, airTrafficControl: ActorRef, groundControl: ActorRef) {

  def stop(context: ActorContext) {
    context stop airTrafficControl
    context stop groundControl
  }
}
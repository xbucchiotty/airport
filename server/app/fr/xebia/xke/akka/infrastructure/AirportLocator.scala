package fr.xebia.xke.akka.infrastructure

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
import fr.xebia.xke.akka.infrastructure.AirportLocator.AirportAddressLookup
import fr.xebia.xke.akka.airport.AirportCode

class AirportLocator(userStore: ActorRef, gameStore: ActorRef) extends Actor with ActorLogging {

  var table: Map[AirportCode, (Address, ActorRef)] = _

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

    case AirportAddressLookup(airportCode) =>
      sender ! table.get(airportCode).map(_._1)
  }

  def memberIsPlayer(member: Member) = member.roles.contains("player")

  def register(member: Member) {
    import context.dispatcher
    implicit val timeout = Timeout(1.second)

    log.info(s"player $member comes in")

    member.roles - "player" map AirportCode foreach (airportCode => {
      table += (airportCode ->(member.address, context.actorOf(AirportProxy.props(member.address), airportCode.toString)))

      val userInfoRequest = ask(userStore, UserStore.AskForAirport(airportCode)).mapTo[Option[UserInfo]]

      userInfoRequest.onSuccess {
        case Some(userInfo) => gameStore ! PlayerUp(userInfo.userId, member.address)
      }
    })
  }

  def unregister(member: Member) {
    import context.dispatcher
    implicit val timeout = Timeout(1.second)

    log.warning(s"player $member moved out")

    member.roles - "player" map AirportCode foreach (airportCode => {
      val proxy = table(airportCode)._2
      context.stop(proxy)
      table -= airportCode

      val userInfoRequest = ask(userStore, UserStore.AskForAirport(airportCode)).mapTo[Option[UserInfo]]

      userInfoRequest.onSuccess {
        case Some(userInfo) => gameStore ! PlayerDown(userInfo.userId, member.address)
      }
    })

  }

}

object AirportLocator {

  def props(userStore: ActorRef, gameStore: ActorRef): Props = Props(classOf[AirportLocator], userStore, gameStore)

  case class AirportAddressLookup(airportCode: AirportCode)

}

class AirportProxy(remoteAddress: Address) extends Actor with ActorLogging {

  var groundControl: ActorRef = _
  var airTrafficControl: ActorRef = _

  override def preStart() {
    airTrafficControl = context.actorOf(SimpleProxy.props(airTrafficControlPath), "airTrafficControl")
    groundControl = context.actorOf(SimpleProxy.props(groundControlPath), "groundControl")
    log.info(s"Start proxy from ${self.path} to $remoteAddress")
  }

  override def postStop() {
    log.info(s"Stop proxy from ${self.path} to $remoteAddress")
  }

  def groundControlPath: ActorSelection = {
    context.actorSelection(ActorPath.fromString(remoteAddress.toString) / "user" / "airport" / "groundControl")
  }

  def airTrafficControlPath: ActorSelection = {
    context.actorSelection(ActorPath.fromString(remoteAddress.toString) / "user" / "airport" / "airTrafficControl")
  }

  def receive: Receive = {
    case _ =>
  }

}

object AirportProxy {

  def props(remoteAddress: Address): Props = Props(classOf[AirportProxy], remoteAddress)

}

class SimpleProxy(remoteLookup: ActorSelection) extends Actor with ActorLogging {

  def receive: Receive = {
    case any => remoteLookup.tell(any, sender)
  }
}

object SimpleProxy {
  def props(remoteLookup: ActorSelection): Props = Props(classOf[SimpleProxy], remoteLookup)
}
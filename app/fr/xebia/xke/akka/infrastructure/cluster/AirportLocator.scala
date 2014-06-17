package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member, MemberStatus}
import akka.pattern.ask
import language.postfixOps
import concurrent.duration._
import akka.util.Timeout
import fr.xebia.xke.akka.airport.AirportCode
import fr.xebia.xke.akka.infrastructure.SessionId
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator._
import akka.event.EventStream
import akka.cluster.ClusterEvent.MemberUp
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator.CreateClient
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator.UpdateClient
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator.AirportDisconnected
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.UnreachableMember
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator.AirportConnected
import fr.xebia.xke.akka.airport.message.{ChaosMonkey, GameInstance, NewGameInstance}

class AirportLocator(clusterEventStream: EventStream) extends Actor with ActorLogging {

  var airportLocations: Map[AirportCode, Address] = _
  var gameSessions: Map[SessionId, AirportProxy] = _

  override def preStart() {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])
    airportLocations = Map.empty
    gameSessions = Map.empty
  }

  def receive: Receive = {
    case state: CurrentClusterState =>
      state.members
        .filter(_.status == MemberStatus.Up)
        .foreach(register)

    case MemberUp(member) =>
      register(member)

    case UnreachableMember(member) =>
      unregister(member)

    case CreateClient(airportCode, sessionId) =>
      createClient(airportCode, sessionId)

    case UpdateClient(airportCode, sessionId, address) =>
      updateClient(airportCode, sessionId, address)

    case AskAddress(airportCode) =>
      sender() ! airportLocations.get(airportCode)

  }

  def createClient(airportCode: AirportCode, sessionId: SessionId) {
    import context.dispatcher
    implicit val timeout: Timeout = Timeout(10 seconds)

    for (address <- airportLocations.get(airportCode)) {
      val airportManager = context.actorSelection(ActorPath.fromString(address.toString) / "user" / airportCode.toString)

      val lastSender = sender()
      for (gameInstance <- ask(airportManager, NewGameInstance(sessionId.toString)).mapTo[GameInstance]) {

        val atcProxy = context.actorOf(RemoteProxy.props(gameInstance.airTrafficControlRef), s"$airportCode-$sessionId-airTrafficControl")

        gameSessions += (sessionId -> AirportProxy(address, atcProxy))

        lastSender ! atcProxy
      }
    }
  }

  def updateClient(airportCode: AirportCode, sessionId: SessionId, address: Address) {
    import context.dispatcher
    implicit val timeout: Timeout = Timeout(10 seconds)

    val airportManager = context.actorSelection(ActorPath.fromString(address.toString) / "user" / airportCode.toString)

    for (gameInstance <- ask(airportManager, NewGameInstance(sessionId.toString)).mapTo[GameInstance]) {

      for (gameSession <- gameSessions.get(sessionId)) {
        gameSession.airTrafficControl ! RemoteProxy.Register(gameInstance.airTrafficControlRef)

      }
    }
  }

  def register(member: Member) {

    for (role <- member.roles if role.nonEmpty) {
      val airportCode = AirportCode(role)

      clusterEventStream.publish(AirportConnected(airportCode, member.address))

      log.info(s"<$member> comes in for <$airportCode>")

      if (airportLocations.isDefinedAt(airportCode)) {
        log.info(s"Redirecting proxy for <$airportCode> to <${member.address}>")
      } else {
        log.info(s"Start proxy to <${member.address}>")
      }

      airportLocations += (airportCode -> member.address)
    }
  }

  def unregister(member: Member) {
    implicit val timeout: Timeout = Timeout(10.seconds)

    log.warning(s"$member moved out")

    for {
      role <- member.roles if role.nonEmpty
      airportCode = AirportCode(role)
      currentAddress <- airportLocations.get(airportCode) if currentAddress == member.address
    } {
      clusterEventStream.publish(AirportDisconnected(airportCode, member.address))
      airportLocations -= airportCode
    }
  }

  def memberIsPlayer(member: Member) = member.roles.nonEmpty
}

object AirportLocator {

  def props(clusterEventStream: EventStream): Props = Props(classOf[AirportLocator], clusterEventStream)

  case class CreateClient(airportCode: AirportCode, sessionId: SessionId)

  case class UpdateClient(airportCode: AirportCode, sessionId: SessionId, address: Address)

  case class AirportConnected(airportCode: AirportCode, address: Address)

  case class AirportDisconnected(airportCode: AirportCode, address: Address)

  case class AskAddress(airportCode: AirportCode)

}

case class AirportProxy(address: Address, airTrafficControl: ActorRef) {

  def stop(context: ActorContext) {
    context stop airTrafficControl
  }
}
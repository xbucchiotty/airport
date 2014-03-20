package fr.xebia.xke.akka.airport.game

import akka.actor._
import fr.xebia.xke.akka.airport.game.PlayerStore.{UnboundActorSystem, BoundActorSystem}

class AirportLocator extends Actor {

  var table: Map[String, ActorRef] = _

  override def preStart() {
    table = Map.empty
  }

  def receive: Receive = {
    case BoundActorSystem(address, airport) =>
      table += (airport.code -> context.actorOf(AirportProxy.props(address), name = airport.code))

    case UnboundActorSystem(address, airport) =>
      val proxy = table(airport.code)
      context.stop(proxy)
      table -= airport.code
  }
}

object AirportLocator {

  def props(): Props = Props[AirportLocator]
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
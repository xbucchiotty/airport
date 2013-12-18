package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.Landed

class Runway extends Actor with ActorLogging {

  val free: Receive = {
    case Landed =>
      log.info("Plane <{}> landed on runway <{}>", sender.path.name, self.path.name)
      context become occupied(sender)
  }

  def occupied(staying: ActorRef): Receive = {
    case Landed =>
      log.error("Collision on runway {} between {} and {}", self.path.name, staying.path.name, sender.path.name)
      context stop self
  }

  def receive: Receive = free

}

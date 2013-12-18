package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.Landed

class Runway extends Actor with ActorLogging {

  val free: Receive = {
    case Landed(landed) =>
      context become occupied(landed)
  }

  def occupied(staying: ActorRef): Receive = {
    case Landed(landed) =>
      log.info("Collision on runway between {} and {}", staying.path.name, landed.path.name)
      context stop self
  }

  def receive: Receive = free

}

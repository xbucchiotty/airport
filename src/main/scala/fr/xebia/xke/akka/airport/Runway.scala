package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Message.Landing

class Runway extends Actor with ActorLogging {

  val free: Receive = {
    case Landing(landed) =>
      context become occupied(landed)
  }

  def occupied(staying: Plane): Receive = {
    case Landing(landed) =>
      log.info("Collision on runway between {} and {}", staying, landed)
      context stop self
  }

  def receive: Receive = free

}

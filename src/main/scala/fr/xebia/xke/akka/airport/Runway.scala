package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.Landed

class Runway(airControl: ActorRef) extends Actor with ActorLogging {

  val free: Receive = {

    case msg@Landed(plane) =>
      log.info("Plane <{}> landed on runway <{}>", plane.path.name, self.path.name)

      airControl forward msg

      context become occupied(plane)
  }

  def occupied(staying: ActorRef): Receive = {

    case Landed(other) =>
      log.error("Collision on runway <{}> between <{}> and <{}>", self.path.name, staying.path.name, other.path.name)

      context stop self
  }

  def receive: Receive = free

}

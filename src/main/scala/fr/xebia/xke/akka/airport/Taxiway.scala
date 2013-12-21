package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor, ActorRef}
import fr.xebia.xke.akka.airport.Event.Entered

class Taxiway(capacity: Int, groundControl: ActorRef) extends Actor with ActorLogging {
  assert(capacity > 0)


  def available(free: Int): Receive = {
    case msg@Entered(plane, _) =>
      log.info("Plane <{}> runs on taxiway <{}>", plane.path.name, self.path.name)
      groundControl forward msg

      if (free < 1) {
        context become full
      } else {
        context become available(free - 1)
      }
  }

  val full: Receive = {
    case msg@Entered(plane, _) =>
      log.error("Plane <{}> runs on a full taxiway <{}>", plane.path.name, self.path.name)
      context stop self
  }


  def receive: Receive = available(capacity)

}

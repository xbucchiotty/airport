package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.HasParked

class Gate extends Actor with ActorLogging {

  def free: Receive = {
    case HasParked(plane, _) =>
      log.info("Plane <{}> parked on gate <{}>", plane.path.name, self.path.name)
      context become occupied(plane)
  }

  def occupied(plane: ActorRef): Receive = {
    case HasParked(newPlane, _) => {
      log.error("Collision on gate <{}> between <{}> and <{}>", self.path.name, plane.path.name, newPlane.path.name)
      context stop self
    }
  }

  def receive: Receive = free
}

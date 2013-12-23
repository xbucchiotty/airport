package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.{HasLeft, HasParked}

class Gate(groundControl: ActorRef) extends Actor with ActorLogging {

  def free: Receive = {
    case msg@HasParked(plane, gate) if gate == self =>
      log.info("Plane <{}> parked on gate <{}>", plane.path.name, self.path.name)

      groundControl forward msg

      context become occupied(plane)
  }

  def occupied(plane: ActorRef): Receive = {
    case HasParked(newPlane, gate) if gate == self => {
      log.error("Collision on gate <{}> between <{}> and <{}>", self.path.name, plane.path.name, newPlane.path.name)
      context stop self
    }

    case msg@HasLeft(leavingPlane, gate) if gate == self && leavingPlane == plane => {
      log.info("Plane <{}> leaves gate <{}>", plane.path.name, self.path.name)

      groundControl forward msg

      context become free

    }
  }

  def receive: Receive = free
}

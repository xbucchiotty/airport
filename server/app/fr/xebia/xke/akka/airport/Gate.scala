package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{Collision, HasLeft, HasParked}

class Gate extends Actor with ActorLogging {

  def free: Receive = {
    case HasParked =>
      val plane = sender
      log.debug("Plane <{}> parked on gate <{}>", plane.path.name, self.path.name)

      context become occupied(plane)
  }

  def occupied(plane: ActorRef): Receive = {
    case HasParked => {
      val newPlane = sender

      plane ! Collision(newPlane, self)
      newPlane ! Collision(plane, self)

      log.debug("Collision on gate <{}> between <{}> and <{}>", self.path.name, plane.path.name, newPlane.path.name)
      context stop self
    }

    case HasLeft if sender == plane => {
      log.debug("Plane <{}> leaves gate <{}>", plane.path.name, self.path.name)
      context become free
    }
  }

  def receive: Receive = free
}

object Gate {

  def props() = Props[Gate]
}
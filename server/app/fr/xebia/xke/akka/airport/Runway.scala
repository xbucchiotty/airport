package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{Collision, HasLanded, HasLeft}

class Runway extends Actor with ActorLogging {

  val free: Receive = {

    case HasLanded =>
      val plane = sender
      log.debug("Plane <{}> landed on runway <{}>", plane.path.name, self.path.name)

      context become occupied(plane)

    case HasLeft =>
      val plane = sender
      log.debug("Runway {} free but plane {} has left", self.path.name, plane.path.name)

      context stop self

  }

  def occupied(staying: ActorRef): Receive = {

    case HasLeft if sender == staying =>
      val plane = sender
      log.debug("Plane <{}> has left runway <{}>", plane.path.name, self.path.name)

      context become free

    case HasLanded =>
      val other = sender

      staying ! Collision(other, self)
      other ! Collision(staying, self)

      log.debug("Collision on runway <{}> between <{}> and <{}>", self.path.name, staying.path.name, other.path.name)

      context stop self
  }

  def receive: Receive = free

}

object Runway {

  def props() = Props[Runway]
}
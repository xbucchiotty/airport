package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.{HasLanded, HasLeft}

class Runway(airControl: ActorRef) extends Actor with ActorLogging {

  val free: Receive = {

    case msg@HasLanded(plane, location) if location == self =>
      log.info("Plane <{}> landed on runway <{}>", plane.path.name, self.path.name)

      airControl forward msg

      context become occupied(plane)

    case HasLeft(plane, location) /*if location == self*/ =>
      log.error("Runway {} free but plane {} has left", self.path.name, plane.path.name)

      context stop self

  }

  def occupied(staying: ActorRef): Receive = {

    case msg@HasLeft(plane, location) if staying == plane && location == self =>
      log.info("Plane <{}> has left runway <{}>", plane.path.name, self.path.name)

      airControl forward msg

      context become free

    case HasLanded(other, location) if location == self =>
      log.error("Collision on runway <{}> between <{}> and <{}>", self.path.name, staying.path.name, other.path.name)

      context stop self
  }

  def receive: Receive = free

}

package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Event.Parked

class Gate extends Actor with ActorLogging {

  println("\ninit")

  def free: Receive = {
    case Parked =>
      log.info("Plane <{}> parked on gate <{}>", sender.path.name, self.path.name)
      context become occupied(sender)
  }

  def occupied(plane: ActorRef): Receive = {
    case Parked => {
      log.error("Collision on gate <{}> between <{}> and <{}>", self.path.name, plane.path.name, sender.path.name)
      context stop self
    }
  }

  def receive: Receive = free
}

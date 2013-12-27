package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.GameEvent.{HasParked, HasLeft, StartTaxi, Incoming}

class GroundControl(taxiway: ActorRef, gate: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case Incoming =>
      sender ! Command.TaxiAndPark(taxiway, gate)
      log.info(s"plane ${sender.path.name} requests to park!")

    case StartTaxi =>
      log.info(s"plane ${sender.path.name} has entered the taxiway")

    case HasParked =>
      log.info(s"plane ${sender.path.name} has parked")

    case HasLeft =>
      log.info(s"plane ${sender.path.name} has left!")
  }
}

package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasParked, HasLeft, StartTaxi, Incoming}

class GroundControl(taxiway: ActorRef, gate: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case Incoming =>
      sender ! Command.TaxiAndPark(taxiway, gate)

    case StartTaxi =>

    case HasParked =>

    case HasLeft =>

  }
}

package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{Taxiing, HasParked, HasLeft, Incoming}

class GroundControl(taxiway: ActorRef, gate: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case Incoming =>
      //sender ! TaxiAndPark(taxiway, gate)

    case Taxiing =>

    case HasParked =>

    case HasLeft =>

  }
}

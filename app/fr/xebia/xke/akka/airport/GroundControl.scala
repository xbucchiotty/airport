package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{Taxiing, HasParked, HasLeft, Incoming}
import fr.xebia.xke.akka.airport.Command.TaxiAndPark

class GroundControl(taxiway: Seq[ActorRef], gate: Seq[ActorRef]) extends Actor with ActorLogging {

  def receive: Receive = {
    case Incoming =>
      sender ! TaxiAndPark(taxiway.head, gate.head)

    case Taxiing =>

    case HasParked =>

    case HasLeft =>

  }
}

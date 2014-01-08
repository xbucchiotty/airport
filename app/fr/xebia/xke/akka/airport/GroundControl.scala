package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Command.{ParkAt, Taxi}
import fr.xebia.xke.akka.airport.PlaneEvent.{EndOfTaxi, Taxiing, HasParked, HasLeft, Incoming}

class GroundControl(taxiway: Seq[ActorRef], gate: Seq[ActorRef]) extends Actor with ActorLogging {

  def receive: Receive = {
    case Incoming =>
      sender ! Taxi(taxiway.head)

    case Taxiing =>

    case EndOfTaxi =>
      sender ! ParkAt(gate.head)

    case HasParked =>

    case HasLeft =>

  }
}

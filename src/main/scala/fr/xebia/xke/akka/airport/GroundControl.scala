package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.Command.TaxiAndPark
import fr.xebia.xke.akka.airport.Event.{HasParked, HasLeft, HasEntered, Incoming}

class GroundControl(taxiway: ActorRef, gate: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case Incoming =>
      sender ! TaxiAndPark(taxiway, gate)

    case HasEntered =>
      val plane = sender
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has entered a taxiway)")

    case HasLeft =>
      val plane = sender
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has left a taxiway)")

    case HasParked=>
      val plane = sender
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has left a taxiway)")
  }
}

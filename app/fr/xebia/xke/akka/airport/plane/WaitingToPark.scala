package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.Command.{TaxiAndPark, Contact}
import fr.xebia.xke.akka.airport.PlaneEvent.{StartTaxi, HasLeft, Incoming, TaxiingToGate}

trait WaitingToPark extends PlaneState {

  def airControl: ActorRef


  def waitingToPark(runway: ActorRef) = GameReceive {
    case Contact(groundControl) =>
      replyTo(airControl) {
        groundControl ! Incoming
      }

    case TaxiAndPark(taxiway, gate) =>
      val groundControl = sender

      replyTo(groundControl) {

        runway ! HasLeft
        airControl ! HasLeft
        taxiway ! TaxiingToGate(gate)
        groundControl ! StartTaxi

        updateStep("taxiway")
        context become taxiing(groundControl, taxiway, gate)
      }
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, gate: ActorRef): GameReceive

}

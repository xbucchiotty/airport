package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.Command.{TaxiAndPark, Contact}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, Taxiing}

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
        taxiway ! Taxiing
        groundControl ! Taxiing

        updateStep("taxiway", s"on taxiway ${taxiway.path.name} for ${gate.path.name}")
        context become taxiing(groundControl, taxiway, gate)
      }
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, gate: ActorRef): GameReceive

}

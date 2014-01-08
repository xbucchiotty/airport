package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.Command.{Taxi, Contact}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, Taxiing}

trait WaitingForTaxiway extends PlaneState {

  def airControl: ActorRef

  def waitingToPark(runway: ActorRef) = GameReceive {
    case Contact(groundControl) =>
      replyTo(airControl, Contact(groundControl).toString) {
        groundControl ! Incoming
      }

    case Taxi(taxiway) =>
      val groundControl = sender

      replyTo(groundControl, Taxi(taxiway).toString) {

        runway ! HasLeft
        airControl ! HasLeft
        taxiway ! Taxiing

        updateStep("taxiway", s"on ${taxiway.path.name}")
        context become taxiing(groundControl, taxiway)
      }
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef): GameReceive

}

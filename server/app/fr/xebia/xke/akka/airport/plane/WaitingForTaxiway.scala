package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, Taxiing}
import fr.xebia.xke.akka.airport.command.{Taxi, Contact}

trait WaitingForTaxiway extends PlaneState {

  def airControl: ActorRef

  def waitingToPark(runway: ActorRef) = GameReceive {
    case Contact(groundControl) =>
      reply(detail = Contact(groundControl).toString)(newState = incoming(runway, groundControl)) {
        groundControl ! Incoming
      }
  }

  def incoming(runway: ActorRef, groundControl: ActorRef): Receive = {
    case Contact(_) =>

    case Taxi(taxiway) =>
      reply(detail = Taxi(taxiway).toString)(newState = leavingRunway) {

        runway ! HasLeft
        airControl ! HasLeft
        taxiway ! Taxiing

        updateStep("taxiway", s"on ${taxiway.path.name}")
        context become taxiing(groundControl, taxiway)
      }
  }

  def leavingRunway: Receive = {
    case Taxi(_) =>
  }


  def taxiing(groundControl: ActorRef, taxiway: ActorRef): GameReceive

}

trait LandingAsLastStep extends PlaneState {

  def airControl: ActorRef

  def waitingToPark(runway: ActorRef) = GameReceive {
    case Contact(groundControl) =>
      reply(detail = "Done")(newState = incoming) {

        runway ! HasLeft
        airControl ! HasLeft

        updateStep("done", "Runway left")

        context stop self
      }
  }

  def incoming: Receive = {
    case Contact(_) =>
  }
}
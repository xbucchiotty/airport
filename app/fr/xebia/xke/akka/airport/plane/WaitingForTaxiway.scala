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

trait LandingAsLastStep extends PlaneState {

  def airControl: ActorRef

  def waitingToPark(runway: ActorRef) = GameReceive {
    case Contact(groundControl) =>
      import context.dispatcher
      context.system.scheduler.scheduleOnce(settings.aLandingDuration, new Runnable {

        def run() {
          replyTo(airControl, "Done") {

            runway ! HasLeft
            airControl ! HasLeft

            updateStep("done", "Runway left")

            context stop self
          }

        }
      })
  }
}
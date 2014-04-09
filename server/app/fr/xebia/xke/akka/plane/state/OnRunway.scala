package fr.xebia.xke.akka.plane.state

import akka.actor.{Cancellable, ActorRef}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, Taxiing}
import fr.xebia.xke.akka.airport.command.{Taxi, Contact}
import fr.xebia.xke.akka.plane.Plane
import languageFeature.postfixOps
import concurrent.duration._

private[plane] trait OnRunwayWaitingForTaxiway extends Plane with RadioCommunication {

  private var repeatIncomingTask: Option[Cancellable] = None

  def waitingToTaxi(airControl: ActorRef, runway: ActorRef) = State("runway", LoggingReceive {

    case Contact(groundControl) =>

      replyWithRadio(() => {

        transitionTo(transition = () => {

          import context.dispatcher
          groundControl ! Incoming
          repeatIncomingTask = Some(context.system.scheduler.schedule(10 seconds, 10 seconds, groundControl, Incoming))

        })(nextState = incoming(airControl, runway, groundControl))
      })
  })

  def incoming(airControl: ActorRef, runway: ActorRef, groundControl: ActorRef) = State("runway", LoggingReceive {
    case Taxi(taxiway) =>

      replyWithRadio(() => {

        for (task <- repeatIncomingTask if !task.isCancelled) {
          task.cancel()
        }

        runway ! HasLeft
        taxiway ! Taxiing


        transitionTo(transition = () => {

          airControl ! HasLeft

        })(nextState = taxiing(groundControl, taxiway))
      })
  })

  def taxiing(groundControl: ActorRef, taxiway: ActorRef): State

}

private[plane] trait LandingAsLastStep extends Plane with RadioCommunication {

  def waitingToTaxi(airControl: ActorRef, runway: ActorRef) = State("runway", LoggingReceive {
    case Contact(groundControl) =>
      replyWithRadio(() => {
        runway ! HasLeft

        airControl ! HasLeft

        done()
      })
  })
}
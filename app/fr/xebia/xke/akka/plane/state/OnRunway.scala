package fr.xebia.xke.akka.plane.state

import akka.actor.{Cancellable, ActorRef}
import fr.xebia.xke.akka.airport.message.PlaneEvent
import PlaneEvent.{HasLeft, Incoming, Taxiing}
import fr.xebia.xke.akka.airport.message.command.{Taxi, Contact}
import fr.xebia.xke.akka.plane.Plane
import languageFeature.postfixOps
import concurrent.duration._

private[plane] trait OnRunway extends Plane with RadioCommunication {

  private var repeatIncomingTask: Option[Cancellable] = None

  def waitingToTaxi(airControl: ActorRef, runway: ActorRef) = State("runway", LoggingReceive {

    case Contact(groundControl) =>

      replyWithRadio(() => {

        transitionTo(transition = () => {

          import context.dispatcher
          repeatIncomingTask = Some(context.system.scheduler.schedule(Duration.Zero, 10 seconds, groundControl, Incoming))

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
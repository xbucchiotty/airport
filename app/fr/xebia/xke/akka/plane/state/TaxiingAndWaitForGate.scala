package fr.xebia.xke.akka.plane.state

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.message.PlaneEvent
import PlaneEvent.{HasLeft, EndOfTaxi, HasParked}
import fr.xebia.xke.akka.airport.message.command.ParkAt
import fr.xebia.xke.akka.plane.Plane

private[plane] trait TaxiingAndWaitForGate extends Plane with RadioCommunication {

  def taxiing(groundControl: ActorRef, taxiway: ActorRef) = State("taxiway", LoggingReceive {
    case EndOfTaxi =>
      transitionTo(transition = () => {
        groundControl ! EndOfTaxi
      })(nextState = waitingToPark(taxiway, groundControl))

  })

  def waitingToPark(taxiway: ActorRef, groundControl: ActorRef) = State("taxiway", LoggingReceive {

    case ParkAt(gate) =>
      replyWithRadio(() => {

          import context.dispatcher
          context.system.scheduler.scheduleOnce(settings.anUnloadingPassengersDuration, self, PassengerUnloaded)

          taxiway ! HasLeft
          gate ! HasParked

        transitionTo(transition = () => {

          groundControl ! HasParked

        })(nextState = unloadingPassengers(groundControl, gate))
      })
  })

  def unloadingPassengers(groundControl: ActorRef, destination: ActorRef): State

}

private[plane] case object PassengerUnloaded
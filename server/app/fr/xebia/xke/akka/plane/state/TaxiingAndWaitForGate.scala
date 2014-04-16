package fr.xebia.xke.akka.plane.state

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, EndOfTaxi, HasParked}
import fr.xebia.xke.akka.airport.command.ParkAt
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

private[plane] trait TaxiingAsLastStep extends Plane with RadioCommunication {

  def taxiing(groundControl: ActorRef, taxiway: ActorRef) = State("taxiway", LoggingReceive {

    case EndOfTaxi =>

      groundControl ! HasParked
      taxiway ! HasLeft

      done()

  })

}

private[plane] case object PassengerUnloaded


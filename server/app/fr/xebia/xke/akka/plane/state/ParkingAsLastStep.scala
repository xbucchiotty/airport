package fr.xebia.xke.akka.plane.state

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.{Incoming, RequestTakeoff, HasLeft}
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.airport.command.{Land, Takeoff}

private[plane] trait ParkingAsLastStep extends Plane with RadioCommunication {

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef) =
    State("gate", LoggingReceive {
      case PassengerUnloaded =>

        groundControl ! HasLeft
        gate ! HasLeft

        done()

    })

}

private[plane] trait ParkingAndRequestTakeoff extends Plane with RadioCommunication {

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef) =
    State("gate", LoggingReceive {
      case PassengerUnloaded =>

        transitionTo(transition = () => {
          groundControl ! RequestTakeoff
          gate ! HasLeft
        })(nextState = waitingToTakeoff(groundControl))

    })

  def waitingToTakeoff(groundControl: ActorRef) = State("gate", LoggingReceive {
    case Takeoff(destination) =>


      transitionTo(transition = () => {
        destination ! Incoming
      })(nextState = waitingToLand(groundControl))
  })

  def waitingToLand(groundControl: ActorRef) = State("gate", LoggingReceive {
    case Land(runway) =>

      groundControl ! HasLeft

  })

}
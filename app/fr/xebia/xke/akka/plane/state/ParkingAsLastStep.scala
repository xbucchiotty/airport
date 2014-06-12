package fr.xebia.xke.akka.plane.state

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.message.PlaneEvent
import PlaneEvent.HasLeft
import fr.xebia.xke.akka.plane.Plane
import languageFeature.postfixOps

private[plane] trait ParkingAsLastStep extends Plane with RadioCommunication {

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef) =
    State("gate", LoggingReceive {
      case PassengerUnloaded =>

        groundControl ! HasLeft
        gate ! HasLeft

        done()

    })

}


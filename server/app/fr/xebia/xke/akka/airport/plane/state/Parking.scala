package fr.xebia.xke.akka.airport.plane.state

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.HasLeft
import fr.xebia.xke.akka.airport.plane.Plane

private[plane] trait Parking extends Plane with RadioCommunication {

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef) =
    State("gate", LoggingReceive {
      case Done =>

        groundControl ! HasLeft
        gate ! HasLeft

        done()

    })

}
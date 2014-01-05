package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.HasLeft

trait Parking extends PlaneState {

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef) = GameReceive {

    case Done =>

      groundControl ! HasLeft
      gate ! HasLeft

      updateStatus("Passengers unloaded")
      updateStep("done")

      context stop self
  }

}
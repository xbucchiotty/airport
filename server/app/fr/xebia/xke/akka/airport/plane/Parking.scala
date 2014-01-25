package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.HasLeft

trait Parking extends PlaneState {

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef) = GameReceive {

    case Done =>

      groundControl ! HasLeft
      gate ! HasLeft

      updateStep("done", "Passengers unloaded")

      context stop self
  }

}
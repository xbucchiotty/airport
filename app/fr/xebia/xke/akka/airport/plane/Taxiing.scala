package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.HasParked

trait Taxiing extends PlaneState {

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, destination: ActorRef) = GameReceive {
    case HasParked =>
      groundControl ! HasParked
      import context.dispatcher

      context become unloadingPassengers(groundControl, destination)

      updateStep("gate")

      context.system.scheduler.scheduleOnce(settings.anUnloadingPassengersDuration, self, Done)
  }

  def unloadingPassengers(groundControl: ActorRef, destination: ActorRef): GameReceive

}

case object Done


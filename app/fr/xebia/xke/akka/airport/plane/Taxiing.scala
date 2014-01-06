package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.{EndOfTaxi, HasParked}

trait Taxiing extends PlaneState {

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, gate: ActorRef) = GameReceive {
    case EndOfTaxi =>

      groundControl ! HasParked
      gate ! HasParked

      updateStep("gate", s"At ${gate.path.name}")
      context become unloadingPassengers(groundControl, gate)


      import context.dispatcher
      context.system.scheduler.scheduleOnce(settings.anUnloadingPassengersDuration, self, Done)
  }

  def unloadingPassengers(groundControl: ActorRef, destination: ActorRef): GameReceive

}

case object Done


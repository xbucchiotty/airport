package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.Command
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, HasParked}

trait WaitingForGate extends PlaneState {

  def waitingToPark(taxiway: ActorRef, groundControl: ActorRef): GameReceive = {

    case Command.ParkAt(gate) =>

      reply(detail = Command.ParkAt(gate).toString)(newState = parking) {

        taxiway ! HasLeft
        groundControl ! HasParked
        gate ! HasParked

        updateStep("gate", s"At ${gate.path.name}")

        context become unloadingPassengers(groundControl, gate)

        import context.dispatcher
        context.system.scheduler.scheduleOnce(settings.anUnloadingPassengersDuration, self, Done)
      }


  }

  def parking: Receive = {
    case Command.ParkAt(_) =>
  }

  def unloadingPassengers(groundControl: ActorRef, destination: ActorRef): GameReceive

}
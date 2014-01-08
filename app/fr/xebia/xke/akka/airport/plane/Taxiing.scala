package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.{EndOfTaxi, HasParked}

trait Taxiing extends PlaneState {

  def taxiing(groundControl: ActorRef, taxiway: ActorRef) = GameReceive {
    case EndOfTaxi =>

      groundControl ! EndOfTaxi

      context become waitingToPark(taxiway, groundControl)
  }


  def waitingToPark(taxiway: ActorRef, groundControl: ActorRef): GameReceive

}

case object Done


package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case class HasLanded(plane: ActorRef, runway: ActorRef) extends Event

  case class HasParked(plane: ActorRef, gate: ActorRef) extends Event

  case class HasEntered(plane: ActorRef, location: ActorRef) extends Event

  case class HasLeft(plane: ActorRef, previousLocation: ActorRef) extends Event

  case class TaxiingToGate(plane: ActorRef, taxiway: ActorRef, gate: ActorRef) extends Event

  case object Incoming extends Event

}

object Command {

  case class Land(runway: ActorRef) extends Command

  case class Contact(target: ActorRef) extends Command

  case class TaxiAndPark(taxiway: ActorRef, gate: ActorRef) extends Command

}
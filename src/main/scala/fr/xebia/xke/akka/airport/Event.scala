package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case object HasLanded extends Event

  case object HasParked extends Event

  case object StartTaxi extends Event

  case object HasLeft extends Event

  case class TaxiingToGate(gate: ActorRef) extends Event

  case object Incoming extends Event

  case class Score(points: Int) extends Event

}

object Command {

  case class Land(runway: ActorRef) extends Command

  case class Contact(target: ActorRef) extends Command

  case class TaxiAndPark(taxiway: ActorRef, gate: ActorRef) extends Command

}
package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case class Landed(plane: ActorRef, runway: ActorRef) extends Event

  case class Parked(plane: ActorRef, gate: ActorRef) extends Event

  case class Entered(plane: ActorRef, location: ActorRef) extends Event

  case class Left(plane: ActorRef, previousLocation: ActorRef) extends Event

  case object Incoming extends Command

}

object Command {

  case class Land(runway: ActorRef) extends Command

  case class Contact(target: ActorRef) extends Command

  case class Park(taxiway: ActorRef, gate: ActorRef) extends Command

}
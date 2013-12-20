package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case class Landed(plane: ActorRef) extends Event

  case object Parked extends Event

  case class Entered(plane: ActorRef) extends Event

  case object Left extends Event

}

object Command {

  case class Land(runway: ActorRef) extends Command

  case object Wait extends Command

  case class Contact(target: ActorRef) extends Command

  case object Incoming extends Command

}
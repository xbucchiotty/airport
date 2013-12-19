package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case class Landed(plane: ActorRef) extends Event

  case object Parked extends Event

  case object Incoming extends Command

}

object Command {

  case class Land(runway: ActorRef) extends Command

  case object Wait extends Command

}
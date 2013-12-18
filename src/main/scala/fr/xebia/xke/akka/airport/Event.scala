package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case object Landed extends Event

  case object Parked extends Event

}

object Command {

  case class Land(runway: ActorRef) extends Command

}
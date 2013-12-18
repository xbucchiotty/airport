package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Event

trait Command

object Event {

  case class Landed(plane: ActorRef) extends Event

}

object Command {

  case class Land(target: ActorRef) extends Command

}
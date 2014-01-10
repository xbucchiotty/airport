package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait Command

object Command {

  case class Land(runway: ActorRef) extends Command {
    override def toString = s"Land to ${runway.path.name}"
  }

  case class Contact(target: ActorRef) extends Command {
    override def toString = s"Contact ${target.path.name}"
  }

  case class Taxi(taxiway: ActorRef) extends Command {
    override def toString = s"Taxi through ${taxiway.path.name}"
  }

  case class ParkAt(gate: ActorRef) extends Command {
    override def toString = s"Park at ${gate.path.name}"
  }

  case object Ack

}

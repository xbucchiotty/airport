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

  case class TaxiAndPark(taxiway: ActorRef, gate: ActorRef) extends Command {
    override def toString = s"Taxi to ${gate.path.name} through ${taxiway.path.name}"
  }

  case object Ack

}

package fr.xebia.xke.akka.airport.message.command

import akka.actor.ActorRef


trait Command

case class Land(runway: ActorRef) extends Command {
  override def toString = s"Land to ${runway.path.name}"
}

case class Contact(target: ActorRef) extends Command {
  override def toString = s"Contact"
}

case class Taxi(taxiway: ActorRef) extends Command {
  override def toString = s"Taxi through ${taxiway.path.name}"
}

case class ParkAt(gate: ActorRef) extends Command {
  override def toString = s"Park at ${gate.path.name}"
}

case class Takeoff(destination: ActorRef) extends Command {
  override def toString = s"Takeoff to ${destination.path.name}"
}



case object Repeat extends Command {
  override def toString = s"Repeat"
}
package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait PlaneEvent

trait PlaneError extends PlaneEvent

object PlaneEvent {

  case object HasLanded extends PlaneEvent

  case object HasParked extends PlaneEvent

  case object StartTaxi extends PlaneEvent

  case object HasLeft extends PlaneEvent

  case class Collision(otherPlane: ActorRef) extends PlaneError {
    override def toString = s"Collision with ${otherPlane.path.name}"
  }

  case class TaxiingToGate(gate: ActorRef) extends PlaneEvent {
    override def toString = s"Taxiing to ${gate.path.name}"
  }

  case object Incoming extends PlaneEvent

  case object Done extends PlaneEvent

  case object OutOfKerozen extends PlaneError

}
package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait PlaneEvent

trait PlaneError extends PlaneEvent {
  def message: String

  override def toString = message
}

object PlaneEvent {

  case object HasLanded extends PlaneEvent

  case object HasParked extends PlaneEvent

  case object StartTaxi extends PlaneEvent

  case object HasLeft extends PlaneEvent

  case class Collision(otherPlane: ActorRef) extends PlaneError {
    val message = s"Collision with ${otherPlane.path.name}"
  }

  case class TaxiingToGate(gate: ActorRef) extends PlaneEvent {
    override def toString = s"Taxiing to ${gate.path.name}"
  }

  case object Incoming extends PlaneEvent

  case object Done extends PlaneEvent

  case object OutOfKerozen extends PlaneError{
    val message = "Out of kerozen"
  }

}
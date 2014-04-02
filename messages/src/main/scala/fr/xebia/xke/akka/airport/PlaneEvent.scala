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

  case object HasLeft extends PlaneEvent

  case class Collision(otherPlane: ActorRef, location: ActorRef) extends PlaneError {
    val message = s"Collision on ${location.path.name} with ${otherPlane.path.name}"
  }

  case object Taxiing extends PlaneEvent

  case object EndOfTaxi extends PlaneEvent

  case object Incoming extends PlaneEvent

  case object Done extends PlaneEvent

  case object RequestTakeoff extends PlaneEvent

  case object OutOfKerozen extends PlaneError {
    val message = "Out of kerozen"
  }

}
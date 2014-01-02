package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

trait GameEvent

trait UIEvent

trait Command

object GameEvent {

  case object HasLanded extends GameEvent

  case object HasParked extends GameEvent

  case object StartTaxi extends GameEvent

  case object HasLeft extends GameEvent

  case class TaxiingToGate(gate: ActorRef) extends GameEvent

  case object Incoming extends GameEvent

  case class Score(points: Int) extends GameEvent

}

case class PlaneEvent(evt: String, flightName: String) extends UIEvent

object PlaneEvent {
  def crash(flightName: String) = PlaneEvent("crash", flightName)
  def add(flightName: String) = PlaneEvent("add", flightName)
  def landed(flightName: String) = PlaneEvent("landed", flightName)
  def taxi(flightName: String) = PlaneEvent("taxi", flightName)
  def park(flightName: String) = PlaneEvent("park", flightName)
  def leave(flightName: String) = PlaneEvent("leave", flightName)
  def collision(flightName: String) = PlaneEvent("collision", flightName)
}

object Command {

  case class Land(runway: ActorRef) extends Command

  case class Contact(target: ActorRef) extends Command

  case class TaxiAndPark(taxiway: ActorRef, gate: ActorRef) extends Command

  case object Ack

}
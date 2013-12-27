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

case class FlightEvent(evt: String, flightName: String) extends UIEvent

object FlightEvent {
  def crash(flightName: String) = FlightEvent("crash", flightName)
  def add(flightName: String) = FlightEvent("add", flightName)
  def landed(flightName: String) = FlightEvent("landed", flightName)
  def taxi(flightName: String) = FlightEvent("taxi", flightName)
  def park(flightName: String) = FlightEvent("park", flightName)
  def leave(flightName: String) = FlightEvent("leave", flightName)
}

object Command {

  case class Land(runway: ActorRef) extends Command

  case class Contact(target: ActorRef) extends Command

  case class TaxiAndPark(taxiway: ActorRef, gate: ActorRef) extends Command

}
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

case class PlaneEvent(evt: String, flightName: String, detail: String) extends UIEvent {

  lazy val toJson: String = s"""{
    "step" : "$evt" ,
    "flightName" : "$flightName" ,
    "detail" : "$detail"
   }""".stripMargin
}

object PlaneEvent {
  def detail(detail: String)(flightName: String) = PlaneEvent(evt = "", flightName = flightName, detail = detail)

  def crash(flightName: String) = PlaneEvent(evt = "", flightName, detail = "Error: out of kerozen")

  def incoming(flightName: String) = PlaneEvent("incoming", flightName, detail = "Incoming")

  def landed(flightName: String) = PlaneEvent("runway", flightName, detail = "Landed")

  def taxi(flightName: String) = PlaneEvent("taxiway", flightName, detail = "Taxi")

  def park(flightName: String) = PlaneEvent("gate", flightName, detail = "Park")

  def leave(flightName: String) = PlaneEvent("done", flightName, detail = "Ok")

  def collision(flightName: String, collisioned: String) = PlaneEvent("", flightName, detail = s"Error: Collision with $collisioned")
}

object Command {

  case class Land(runway: ActorRef) extends Command

  case class Contact(target: ActorRef) extends Command

  case class TaxiAndPark(taxiway: ActorRef, gate: ActorRef) extends Command

  case object Ack

}
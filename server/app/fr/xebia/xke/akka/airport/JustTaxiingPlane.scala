package fr.xebia.xke.akka.airport

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.plane.{WaitingForTaxiway, EndOfTaxiAsLastStep, Flying}
import akka.event.EventStream

case class JustTaxiingPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream) extends Plane with Flying with WaitingForTaxiway with EndOfTaxiAsLastStep {

  def receive = flying
}

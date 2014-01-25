package fr.xebia.xke.akka.airport

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.plane.{WaitingForTaxiway, EndOfTaxiAsLastStep, Flying}

case class JustTaxiingPlane(airControl: ActorRef, game: ActorRef, settings: Settings) extends Plane with Flying with WaitingForTaxiway with EndOfTaxiAsLastStep {

  def receive = flying
}

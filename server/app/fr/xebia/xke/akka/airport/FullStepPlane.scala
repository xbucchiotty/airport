package fr.xebia.xke.akka.airport

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.plane.{WaitingForGate, Taxiing, Parking, WaitingForTaxiway, Flying}
import languageFeature.postfixOps
import akka.event.EventStream

case class FullStepPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream) extends Plane with Flying with WaitingForTaxiway with Taxiing with WaitingForGate with Parking {

  def receive: Receive = flying

}
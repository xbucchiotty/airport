package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import languageFeature.postfixOps
import akka.event.EventStream
import fr.xebia.xke.akka.airport.Settings
import fr.xebia.xke.akka.airport.plane.state._

case class FullStepPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with OnRunwayWaitingForTaxiway
  with TaxiingAndWaitForGate
  with Parking {

  def initialState = flying
}
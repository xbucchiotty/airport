package fr.xebia.xke.akka.plane

import akka.actor.ActorRef
import akka.event.EventStream
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.plane.state.{TaxiingAsLastStep, OnRunwayWaitingForTaxiway, Incoming}

case class JustTaxiingPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with OnRunwayWaitingForTaxiway
  with TaxiingAsLastStep {

  def initialState = flying
}

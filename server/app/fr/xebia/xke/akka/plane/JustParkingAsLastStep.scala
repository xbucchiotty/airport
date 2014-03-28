package fr.xebia.xke.akka.plane

import akka.actor.{Props, ActorRef}
import languageFeature.postfixOps
import akka.event.EventStream
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.plane.state.{ParkingAsLastStep, TaxiingAndWaitForGate, OnRunwayWaitingForTaxiway, Incoming}

case class JustParkingAsLastStep(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with OnRunwayWaitingForTaxiway
  with TaxiingAndWaitForGate
  with ParkingAsLastStep {

  def initialState = flying
}

object JustParkingAsLastStep {

  def props(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream) =
    Props(classOf[JustParkingAsLastStep], airControl, game, settings, eventStream)
}
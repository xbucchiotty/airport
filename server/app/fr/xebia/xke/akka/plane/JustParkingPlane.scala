package fr.xebia.xke.akka.plane

import akka.actor.{Props, ActorRef}
import languageFeature.postfixOps
import akka.event.EventStream
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.plane.state.{ParkingAsLastStep, TaxiingAndWaitForGate, OnRunwayWaitingForTaxiway, Incoming}

case class JustParkingPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with OnRunwayWaitingForTaxiway
  with TaxiingAndWaitForGate
  with ParkingAsLastStep {

  def initialState = idle
}

object JustParkingPlane {

  def props(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream) =
    Props(classOf[JustParkingPlane], airControl, game, settings, eventStream)
}
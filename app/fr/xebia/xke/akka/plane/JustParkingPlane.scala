package fr.xebia.xke.akka.plane

import akka.actor.Props
import languageFeature.postfixOps
import akka.event.EventStream
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.plane.state.{ParkingAsLastStep, TaxiingAndWaitForGate, OnRunway, Incoming}

case class JustParkingPlane(settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with OnRunway
  with TaxiingAndWaitForGate
  with ParkingAsLastStep {

  def initialState = idle
}

object JustParkingPlane {

  def props(settings: Settings, eventStream: EventStream) =
    Props(classOf[JustParkingPlane], settings, eventStream)
}
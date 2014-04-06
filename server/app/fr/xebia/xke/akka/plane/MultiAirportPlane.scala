package fr.xebia.xke.akka.plane

import akka.actor.{Props, ActorRef}
import languageFeature.postfixOps
import akka.event.EventStream
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.plane.state._

case class MultiAirportPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with OnRunwayWaitingForTaxiway
  with TaxiingAndWaitForGate
  with ParkingAndRequestTakeoff {

  def initialState = idle
}

object MultiAirportPlane {

  def props(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream) =
    Props(classOf[MultiAirportPlane], airControl, game, settings, eventStream)
}
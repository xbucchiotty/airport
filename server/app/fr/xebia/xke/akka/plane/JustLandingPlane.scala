package fr.xebia.xke.akka.plane

import akka.actor.{Props, ActorRef}
import akka.event.EventStream
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.plane.state.{LandingAsLastStep, Incoming}

case class JustLandingPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with LandingAsLastStep {

  def initialState = idle
}

object JustLandingPlane {

  def props(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream) =
    Props(classOf[JustLandingPlane], airControl, game, settings, eventStream)
}
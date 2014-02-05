package fr.xebia.xke.akka.airport.plane

import akka.actor.ActorRef
import akka.event.EventStream
import fr.xebia.xke.akka.airport.Settings
import fr.xebia.xke.akka.airport.plane.state.{LandingAsLastStep, Incoming}

case class JustLandingPlane(airControl: ActorRef, game: ActorRef, settings: Settings, eventStream: EventStream)
  extends Plane
  with Incoming
  with LandingAsLastStep{

  def initialState = flying
}

package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.plane.{LandingAsLastStep, Flying}
import akka.actor.ActorRef

case class JustLandingPlane(airControl: ActorRef, game: ActorRef, settings: Settings) extends Plane with Flying with LandingAsLastStep {

  def receive: Receive = flying

}

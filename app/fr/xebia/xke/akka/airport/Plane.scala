package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Actor}
import fr.xebia.xke.akka.airport.plane.{Taxiing, Parking, WaitingToPark, Flying}
import languageFeature.postfixOps

case class Plane(airControl: ActorRef, game: ActorRef, settings: Settings) extends Actor with Flying with WaitingToPark with Taxiing with Parking {

  def receive: Receive = flying

}
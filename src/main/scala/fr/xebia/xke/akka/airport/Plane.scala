package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Actor}
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.{TaxiAndPark, Land}
import fr.xebia.xke.akka.airport.Event.{HasParked, HasEntered, HasLeft, HasLanded}
import languageFeature.postfixOps
import scala.util.Random

class Plane extends Actor {

  val inTheAir: Receive = {
    case Land(runway) =>

      import context.dispatcher
      context.system.scheduler.scheduleOnce(landingDuration, self, HasLanded(self, runway))

    case msg@HasLanded(plane, runway) if plane == self =>
      if (sender == self)
        runway forward msg
      context become waitingToPark(runway)

  }

  def waitingToPark(runway: ActorRef): Receive = {
    case Command.TaxiAndPark(taxiway, gate) =>
      runway ! HasLeft(self, runway)
      taxiway ! HasEntered(self, taxiway)

      context become taxiing(taxiway, gate)
  }

  def taxiing(taxiway: ActorRef, destination: ActorRef): Receive = {
    case HasParked(plane, gate) if plane == self && gate == destination =>
      context stop self
  }

  def receive: Receive =
    inTheAir

  private def landingDuration: FiniteDuration =
    Duration(
      Random.nextInt(Plane.MAX_LANDING_TIMEOUT),
      MILLISECONDS
    )
}

object Plane {
  val MAX_LANDING_TIMEOUT = 300
}
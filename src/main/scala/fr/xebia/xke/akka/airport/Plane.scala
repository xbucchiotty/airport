package fr.xebia.xke.akka.airport

import akka.actor.Actor
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Land
import languageFeature.postfixOps
import scala.util.Random
import fr.xebia.xke.akka.airport.Event.Landed

class Plane(initiallyInTheAir: Boolean) extends Actor {

  val inTheAir: Receive = {
    case Land(runway) => {
      import context.dispatcher
      context.system.scheduler.scheduleOnce(landingDuration, runway, Landed(self))
    }
  }

  def parked: Receive = {
    ???
  }

  def receive: Receive =
    if (initiallyInTheAir)
      inTheAir
    else
      parked


  private def landingDuration: FiniteDuration =
    Duration(
      Random.nextInt(Plane.MAX_LANDING_TIMEOUT),
      MILLISECONDS
    )
}

object Plane {
  val MAX_LANDING_TIMEOUT = 300
}
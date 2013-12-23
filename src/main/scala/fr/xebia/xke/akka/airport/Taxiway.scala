package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor, ActorRef}
import fr.xebia.xke.akka.airport.Event.{HasEntered, HasLeft, TaxiingToGate, HasParked}
import concurrent.duration._
import languageFeature.postfixOps
import scala.util.Random

class Taxiway(capacity: Int, groundControl: ActorRef) extends Actor with ActorLogging {
  assert(capacity > 0)

  def available(free: Int): Receive = planeLeaves(free) orElse {

    case msg@TaxiingToGate(plane, taxiway, gate) if taxiway == self =>

      log.info("Plane <{}> runs on taxiway <{}>", plane.path.name, self.path.name)
      groundControl ! HasEntered(plane, self)

      val freePlaces = free - 1

      if (freePlaces < 1) {

        log.info("Taxiway {} is now full", self.path.name)
        context become full

      } else {

        log.info("Remains {} places on taxiway {}", freePlaces, self.path.name)
        context become available(freePlaces)

      }

      import context.dispatcher
      context.system.scheduler.scheduleOnce(taxiingDuration, self, HasParked(plane, gate))

  }

  def planeLeaves(free: Int): Receive = {
    case msg@HasParked(plane, gate) =>
      log.info("Plane <{}> leaves taxiway <{}>", plane.path.name, self.path.name)
      groundControl ! HasLeft(plane, self)
      gate ! msg
      context become available(free + 1)
  }

  val full: Receive = planeLeaves(0) orElse {
    case msg@TaxiingToGate(plane, taxiway, _) if taxiway == self =>
      log.error("Plane <{}> runs on a full taxiway <{}>", plane.path.name, self.path.name)
      context stop self
  }


  def receive: Receive = available(capacity)

  private def taxiingDuration: FiniteDuration =
    Duration(
      Random.nextInt(Taxiway.TAXIING_TIMEOUT),
      MILLISECONDS
    )

}

object Taxiway {
  val TAXIING_TIMEOUT = 1000

}
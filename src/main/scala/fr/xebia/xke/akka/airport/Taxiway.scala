package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor, ActorRef}
import fr.xebia.xke.akka.airport.Event.{HasEntered, HasLeft, TaxiingToGate, HasParked}
import concurrent.duration._
import languageFeature.postfixOps
import scala.util.Random
import scala.collection.immutable.Queue

class Taxiway(capacity: Int) extends Actor with ActorLogging {
  assert(capacity > 0)

  private var queue = Queue.empty[(ActorRef, ActorRef)]

  private var free = capacity

  val acceptNewPlane: Receive = {
    case msg@TaxiingToGate(gate) =>

      val plane = sender
      log.info("Plane <{}> runs on taxiway <{}>", plane.path.name, self.path.name)
      this.queue = queue enqueue(plane, msg.gate)

      free -= 1
      if (free < 1) {
        log.info("Taxiway {} is now full", self.path.name)
        context become full
      } else {
        log.info("Remains {} places on taxiway {}", free, self.path.name)
      }
  }

  val rejectNewPlane: Receive = {
    case msg: TaxiingToGate =>
      val plane = sender
      log.error("Plane <{}> runs on a full taxiway <{}>", plane.path.name, self.path.name)
      context stop self

  }

  val planeLeaves: Receive = {
    case this.Tick =>
      if (queue.nonEmpty) {
        val ((plane, gate), newQueue) = queue.dequeue
        this.queue = newQueue

        log.info("Plane <{}> leaves taxiway <{}>", plane.path.name, self.path.name)
        gate ! HasParked
        plane ! HasParked

        free += 1

        context become available
      }

      import context.dispatcher
      context.system.scheduler.scheduleOnce(randomTaxiingDuration, self, Tick)

  }

  def available: Receive = planeLeaves orElse acceptNewPlane

  def full: Receive = planeLeaves orElse rejectNewPlane

  def receive: Receive = available

  private def randomTaxiingDuration: FiniteDuration =
    Duration(Random.nextInt(Taxiway.TAXIING_TIMEOUT), MILLISECONDS)


  case object Tick

  override def preStart() {
    import context.dispatcher

    context.system.scheduler.scheduleOnce(randomTaxiingDuration, self, Tick)
  }
}

object Taxiway {
  val TAXIING_TIMEOUT = 1000
}
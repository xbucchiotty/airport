package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor, ActorRef}
import fr.xebia.xke.akka.airport.Event.{HasEntered, HasLeft, TaxiingToGate, HasParked}
import concurrent.duration._
import languageFeature.postfixOps
import scala.util.Random
import scala.collection.immutable.Queue

class Taxiway(capacity: Int, groundControl: ActorRef) extends Actor with ActorLogging {
  assert(capacity > 0)

  private var queue = Queue.empty[HasParked]

  private var free = capacity

  val acceptNewPlane: Receive = {
    case msg@TaxiingToGate(plane, taxiway, gate) if taxiway == self =>
      log.info("Plane <{}> runs on taxiway <{}>", plane.path.name, self.path.name)

      groundControl ! HasEntered(plane, self)

      this.queue = queue enqueue HasParked(plane, gate)

      free -= 1
      if (free < 1) {
        log.info("Taxiway {} is now full", self.path.name)
        context become full
      } else {
        log.info("Remains {} places on taxiway {}", free, self.path.name)
      }
  }

  val rejectNewPlane: Receive = {
    case msg@TaxiingToGate(plane, taxiway, _) if taxiway == self =>
      log.error("Plane <{}> runs on a full taxiway <{}>", plane.path.name, self.path.name)
      context stop self

  }

  val planeLeaves: Receive = {
    case this.Tick =>
      if (queue.nonEmpty) {
        val (msg, newQueue) = queue.dequeue
        this.queue = newQueue
        import msg._

        log.info("Plane <{}> leaves taxiway <{}>", plane.path.name, self.path.name)
        groundControl ! HasLeft(plane, self)
        gate ! msg

        free += 1

        context become available
      }

      import context.dispatcher
      context.system.scheduler.scheduleOnce(randomTaxiingDuration, self, Tick)

  }

  import context.dispatcher

  context.system.scheduler.scheduleOnce(randomTaxiingDuration, self, Tick)

  def available: Receive = planeLeaves orElse acceptNewPlane

  def full: Receive = planeLeaves orElse rejectNewPlane

  def receive: Receive = available

  private def randomTaxiingDuration: FiniteDuration =
    Duration(Random.nextInt(Taxiway.TAXIING_TIMEOUT), MILLISECONDS)


  case object Tick

}

object Taxiway {
  val TAXIING_TIMEOUT = 1000
}
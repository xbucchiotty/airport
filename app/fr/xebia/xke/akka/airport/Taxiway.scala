package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor, ActorRef}
import fr.xebia.xke.akka.airport.GameEvent.{TaxiingToGate, HasParked}
import languageFeature.postfixOps
import scala.collection.immutable.Queue

class Taxiway(settings: Settings) extends Actor with ActorLogging {

  private var queue = Queue.empty[(ActorRef, ActorRef)]

  private var free = settings.taxiwayCapacity

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
      context.system.eventStream.publish(PlaneEvent.collision(plane.path.name))
      log.error("Plane <{}> runs on a full taxiway <{}>", plane.path.name, self.path.name)
      context stop self

  }

  val dequeueAPlane: Receive = {
    case this.Tick =>
      if (queue.nonEmpty) {
        val ((plane, gate), newQueue) = queue.dequeue
        this.queue = newQueue

        log.info("Plane <{}> leaves taxiway <{}>", plane.path.name, self.path.name)
        gate.tell(HasParked, sender = plane)
        plane ! HasParked

        free += 1

        context become available
      }

      import context.dispatcher
      context.system.scheduler.scheduleOnce(settings.aRandomTaxiingDuration, self, Tick)

  }

  def available: Receive = dequeueAPlane orElse acceptNewPlane

  def full: Receive = dequeueAPlane orElse rejectNewPlane

  def receive: Receive = available

  case object Tick

  override def preStart() {
    import context.dispatcher

    context.system.scheduler.scheduleOnce(settings.aRandomTaxiingDuration, self, Tick)
  }
}
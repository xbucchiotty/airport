package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, EndOfTaxi, Taxiing, Collision}
import languageFeature.postfixOps
import scala.collection.immutable.Queue
import fr.xebia.xke.akka.game.Settings

class Taxiway(settings: Settings) extends Actor with ActorLogging {

  private var queue = Queue.empty[ActorRef]

  private var free = settings.taxiwayCapacity

  val planeHasLeft: Receive = {
    case HasLeft if sender() == queue.head =>
      val (plane, newQueue) = queue.dequeue
      this.queue = newQueue


      log.debug("Plane <{}> leaves taxiway <{}>", plane.path.name, self.path.name)

      free += 1

      context become available

      import context.dispatcher
      context.system.scheduler.scheduleOnce(settings.taxiingDuration milliseconds, self, Tick)
  }

  val acceptNewPlane: Receive = {
    case Taxiing =>
      val plane = sender()
      log.debug("Plane <{}> runs on taxiway <{}>", plane.path.name, self.path.name)
      this.queue = queue enqueue plane

      free -= 1
      if (free < 1) {
        log.debug("Taxiway {} is now full", self.path.name)
        context become full
      } else {
        log.debug("Remains {} places on taxiway {}", free, self.path.name)
      }
  }

  val rejectNewPlane: Receive = {
    case Taxiing =>
      val plane = sender()

      plane ! Collision(queue.last, self)

      log.debug("Plane <{}> runs on a full taxiway <{}>", plane.path.name, self.path.name)

      context stop self

  }

  val dequeueAPlane: Receive = {
    case this.Tick =>
      if (queue.nonEmpty) {
        val plane = queue.head

        plane ! EndOfTaxi

      } else {
        import context.dispatcher

        context.system.scheduler.scheduleOnce(settings.taxiingDuration milliseconds, self, Tick)
      }
  }

  def available: Receive = dequeueAPlane orElse acceptNewPlane orElse planeHasLeft

  def full: Receive = dequeueAPlane orElse rejectNewPlane orElse planeHasLeft

  def receive: Receive = available

  case object Tick

  override def preStart() {
    import context.dispatcher

    context.system.scheduler.scheduleOnce(settings.taxiingDuration milliseconds, self, Tick)
  }
}

object Taxiway {

  def props(settings: Settings) = Props(classOf[Taxiway], settings)
}
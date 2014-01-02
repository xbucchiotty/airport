package fr.xebia.xke.akka.airport

import akka.actor.{Cancellable, ActorLogging, ActorRef, Actor}
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.{Ack, Contact, Land}
import fr.xebia.xke.akka.airport.GameEvent.{Score, HasParked, Incoming, TaxiingToGate, StartTaxi, HasLeft, HasLanded}
import fr.xebia.xke.akka.airport.Plane.{UnloadingTerminated, OutOfKerozen}
import languageFeature.postfixOps

class Plane(airControl: ActorRef, game: ActorRef, settings: Settings) extends Actor with ActorLogging {

  var outOfKerozenCrash: Cancellable = null

  val inTheAir: Receive = {
    case Land(runway) =>
      import context.dispatcher
      val airControl = sender
      context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          airControl ! Ack

          import context.dispatcher
          outOfKerozenCrash.cancel()
          context.system.scheduler.scheduleOnce(settings.aLandingDuration, self, Landed(runway))
        }
      })

    case this.Landed(runway) =>
      context.system.eventStream.publish(PlaneEvent.landed(self.path.name))
      airControl ! HasLanded
      runway ! HasLanded
      context become waitingToPark(runway)

    case OutOfKerozen =>
      context.system.eventStream.publish(PlaneEvent.crash(self.path.name))
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
      context stop self
  }

  def waitingToPark(runway: ActorRef): Receive = {
    case Contact(groundControl) =>
      import context.dispatcher
      val airControl = sender
      context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          airControl ! Ack

          groundControl ! Incoming
        }
      })

    case Command.TaxiAndPark(taxiway, gate) =>
      import context.dispatcher
      val groundControl = sender
      context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          groundControl ! Ack

          context.system.eventStream.publish(PlaneEvent.taxi(self.path.name))

          runway ! HasLeft
          airControl ! HasLeft
          taxiway ! TaxiingToGate(gate)
          groundControl ! StartTaxi

          context become taxiing(groundControl, taxiway, gate)
        }
      })
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, destination: ActorRef): Receive = {
    case HasParked =>
      context.system.eventStream.publish(PlaneEvent.park(self.path.name))
      groundControl ! HasParked
      import context.dispatcher
      context.system.scheduler.scheduleOnce(settings.anUnloadingPassengersDuration, self, UnloadingTerminated)
      context become unloadingPassengers(groundControl, destination)
  }

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef): Receive = {
    case UnloadingTerminated =>
      context.system.eventStream.publish(PlaneEvent.leave(self.path.name))
      groundControl ! HasLeft
      gate ! HasLeft
      game ! Score(10)
      context stop self
  }

  def receive: Receive =
    inTheAir

  case class Landed(runway: ActorRef)

  override def preStart() {
    context.system.eventStream.publish(PlaneEvent.add(self.path.name))

    airControl ! Incoming

    import context.dispatcher
    outOfKerozenCrash = context.system.scheduler.scheduleOnce(settings.outOfKerozenTimeout milliseconds, self, OutOfKerozen)
  }
}


object Plane {

  case object OutOfKerozen

  case object UnloadingTerminated


}
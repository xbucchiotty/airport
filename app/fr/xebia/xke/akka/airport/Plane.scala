package fr.xebia.xke.akka.airport

import akka.actor.{Cancellable, ActorLogging, ActorRef, Actor}
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.{Contact, Land}
import fr.xebia.xke.akka.airport.GameEvent.{Score, HasParked, Incoming, TaxiingToGate, StartTaxi, HasLeft, HasLanded}
import fr.xebia.xke.akka.airport.Plane.{UnloadingTerminated, OutOfKerozen}
import languageFeature.postfixOps
import scala.util.Random

class Plane(airControl: ActorRef, game: ActorRef) extends Actor with ActorLogging {

  var outOfKerozenCrash: Cancellable = null

  val inTheAir: Receive = {
    case Land(runway) =>
      import context.dispatcher
      outOfKerozenCrash.cancel()
      context.system.scheduler.scheduleOnce(landingDuration, self, Landed(runway))

    case this.Landed(runway) =>
      airControl ! HasLanded
      runway ! HasLanded
      context.system.eventStream.publish(FlightEvent.landed(self.path.name))
      context become waitingToPark(runway)

    case OutOfKerozen =>
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
      context.system.eventStream.publish(FlightEvent.crash(self.path.name))
      context stop self
  }

  def waitingToPark(runway: ActorRef): Receive = {
    case Contact(groundControl) =>
      groundControl ! Incoming

    case Command.TaxiAndPark(taxiway, gate) =>
      val groundControl = sender
      runway ! HasLeft
      airControl ! HasLeft
      taxiway ! TaxiingToGate(gate)
      groundControl ! StartTaxi

      context.system.eventStream.publish(FlightEvent.taxi(self.path.name))
      context become taxiing(groundControl, taxiway, gate)
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, destination: ActorRef): Receive = {
    case HasParked =>
      groundControl ! HasParked
      import context.dispatcher
      context.system.eventStream.publish(FlightEvent.park(self.path.name))
      context.system.scheduler.scheduleOnce(unloadingDuration, self, UnloadingTerminated)
      context become unloadingPassengers(groundControl, destination)
  }

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef): Receive = {
    case UnloadingTerminated =>
      groundControl ! HasLeft
      gate ! HasLeft
      game ! Score(10)
      context.system.eventStream.publish(FlightEvent.leave(self.path.name))
      context stop self
  }

  def receive: Receive =
    inTheAir

  private def landingDuration: FiniteDuration =
    Duration(
      Random.nextInt(Plane.MAX_LANDING_TIMEOUT),
      MILLISECONDS
    )

  private def unloadingDuration: FiniteDuration =
    Duration(
      Random.nextInt(Plane.MAX_UNLOADING_PASSENGERS_TIMEOUT),
      MILLISECONDS
    )

  case class Landed(runway: ActorRef)

  override def preStart() {
    airControl ! Incoming

    context.system.eventStream.publish(FlightEvent.add(self.path.name))

    import context.dispatcher
    outOfKerozenCrash = context.system.scheduler.scheduleOnce(Plane.OUT_OF_KEROZEN_TIMEOUT milliseconds, self, OutOfKerozen)
  }
}


object Plane {
  val MAX_LANDING_TIMEOUT = 300
  val MAX_UNLOADING_PASSENGERS_TIMEOUT = 5000
  val OUT_OF_KEROZEN_TIMEOUT = 3000

  case object OutOfKerozen

  case object UnloadingTerminated


}
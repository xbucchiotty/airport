package fr.xebia.xke.akka.airport

import akka.actor.{Cancellable, ActorLogging, ActorRef, Actor}
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.{Contact, TaxiAndPark, Land}
import fr.xebia.xke.akka.airport.Event.{Score, HasParked, Incoming, TaxiingToGate, StartTaxi, HasLeft, HasLanded}
import languageFeature.postfixOps
import scala.util.Random
import fr.xebia.xke.akka.airport.Plane.{UnloadingTerminated, OutOfKerozen}

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
      context become waitingToPark(runway)

    case OutOfKerozen =>
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
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

      context become taxiing(groundControl, taxiway, gate)
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, destination: ActorRef): Receive = {
    case HasParked =>
      groundControl ! HasParked
      import context.dispatcher
      context.system.scheduler.scheduleOnce(unloadingDuration, self, UnloadingTerminated)
      context become unloadingPassengers(groundControl, destination)
  }

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef): Receive = {
    case UnloadingTerminated =>
      groundControl ! HasLeft
      gate ! HasLeft
      game ! Score(10)
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
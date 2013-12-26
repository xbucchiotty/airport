package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, ActorRef, Actor}
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.{Contact, TaxiAndPark, Land}
import fr.xebia.xke.akka.airport.Event.{HasParked, Incoming, TaxiingToGate, HasEntered, HasLeft, HasLanded}
import languageFeature.postfixOps
import scala.util.Random

class Plane extends Actor with ActorLogging {

  val inTheAir: Receive = {
    case Land(runway) =>

      import context.dispatcher
      val airControl = sender
      context.system.scheduler.scheduleOnce(landingDuration, self, Landed(airControl, runway))

    case this.Landed(airControl, runway) =>
      airControl ! HasLanded
      runway ! HasLanded
      context become waitingToPark(airControl, runway)

    case this.OutOfKerozen =>
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
      context stop self
  }

  def waitingToPark(airControl: ActorRef, runway: ActorRef): Receive = {
    case Contact(groundControl) =>
      groundControl ! Incoming

    case Command.TaxiAndPark(taxiway, gate) =>
      val groundControl = sender
      runway ! HasLeft
      airControl ! HasLeft
      taxiway ! TaxiingToGate(gate)
      groundControl ! HasEntered

      context become taxiing(groundControl, taxiway, gate)
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, destination: ActorRef): Receive = {
    case HasParked =>
      groundControl ! HasParked
      context stop self
  }

  def receive: Receive =
    inTheAir

  private def landingDuration: FiniteDuration =
    Duration(
      Random.nextInt(Plane.MAX_LANDING_TIMEOUT),
      MILLISECONDS
    )

  case object OutOfKerozen

  case class Landed(airControl: ActorRef, runway: ActorRef)

  override def preStart() {
    import context.dispatcher
    context.system.scheduler.scheduleOnce(Plane.OUT_OF_KEROZEN_TIMEOUT milliseconds, self, OutOfKerozen)
  }
}


object Plane {
  val MAX_LANDING_TIMEOUT = 300
  val OUT_OF_KEROZEN_TIMEOUT = 3000
}
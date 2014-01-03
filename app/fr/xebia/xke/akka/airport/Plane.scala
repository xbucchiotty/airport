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
      replyTo(airControl) {
        publish(PlaneEvent.detail("Land ack"))

        import context.dispatcher
        outOfKerozenCrash.cancel()
        context.system.scheduler.scheduleOnce(settings.aLandingDuration, self, Landed(runway))
      }

    case this.Landed(runway) =>
      publish(PlaneEvent.landed)
      airControl ! HasLanded
      runway ! HasLanded
      context become waitingToPark(runway)

    case OutOfKerozen =>
      publish(PlaneEvent.crash)
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
      context stop self
  }

  def waitingToPark(runway: ActorRef): Receive = {
    case Contact(groundControl) =>
      replyTo(airControl) {
        groundControl ! Incoming
      }

    case Command.TaxiAndPark(taxiway, gate) =>
      val groundControl = sender

      replyTo(groundControl) {
        publish(PlaneEvent.taxi)

        runway ! HasLeft
        airControl ! HasLeft
        taxiway ! TaxiingToGate(gate)
        groundControl ! StartTaxi

        context become taxiing(groundControl, taxiway, gate)
      }
  }

  def taxiing(groundControl: ActorRef, taxiway: ActorRef, destination: ActorRef): Receive = {
    case HasParked =>
      publish(PlaneEvent.park)
      groundControl ! HasParked

      import context.dispatcher

      context.system.scheduler.scheduleOnce(settings.anUnloadingPassengersDuration, self, UnloadingTerminated)
      context become unloadingPassengers(groundControl, destination)
  }

  def unloadingPassengers(groundControl: ActorRef, gate: ActorRef): Receive = {
    case UnloadingTerminated =>
      publish(PlaneEvent.leave)
      groundControl ! HasLeft
      gate ! HasLeft
      game ! Score(10)
      context stop self
  }

  def receive: Receive =
    inTheAir


  override def preStart() {
    publish(PlaneEvent.incoming)

    airControl ! Incoming

    import context.dispatcher

    outOfKerozenCrash = context.system.scheduler.scheduleOnce(settings.outOfKerozenTimeout milliseconds, self, OutOfKerozen)
  }

  private def replyTo(target: ActorRef)(command: => Unit) {
    if (settings.isRadioOk) {
      import context.dispatcher
      context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          target ! Ack

          command
        }
      })
    }
  }

  private case class Landed(runway: ActorRef)

  private def publish(event: String => UIEvent) {
    context.system.eventStream.publish(event(self.path.name))
  }
}

object Plane {

  case object OutOfKerozen

  case object UnloadingTerminated

}
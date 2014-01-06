package fr.xebia.xke.akka.airport.plane

import akka.actor.{Cancellable, ActorRef}
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.PlaneEvent.{OutOfKerozen, HasLanded, Incoming}
import languageFeature.postfixOps
import fr.xebia.xke.akka.airport.PlaneEvent

trait Flying extends PlaneState {

  def airControl: ActorRef

  private var outOfKerozenCrash: Cancellable = null

  val flying = GameReceive {
    case Land(runway) =>
      replyTo(airControl) {
        import context.dispatcher
        outOfKerozenCrash.cancel()
        context.system.scheduler.scheduleOnce(settings.aLandingDuration, self, Landed(runway))
      }

    case this.Landed(runway) =>
      airControl ! HasLanded
      runway ! HasLanded

      updateStep("runway", s"On runway ${runway.path.name}")

      context become waitingToPark(runway)

    case OutOfKerozen =>
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
      context stop self
  }

  override def preStart() {
    airControl ! Incoming

    updateStep("incoming", "Hello there")

    import context.dispatcher
    outOfKerozenCrash = context.system.scheduler.scheduleOnce(settings.outOfKerozenTimeout milliseconds, self, OutOfKerozen)
  }


  def waitingToPark(runway: ActorRef): GameReceive

  private case class Landed(runway: ActorRef) extends PlaneEvent {
    override def toString = s"Landed"
  }

}

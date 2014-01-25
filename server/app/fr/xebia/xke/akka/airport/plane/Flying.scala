package fr.xebia.xke.akka.airport.plane

import akka.actor.{Cancellable, ActorRef}
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent
import fr.xebia.xke.akka.airport.PlaneEvent.{OutOfKerozen, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.command.Land
import languageFeature.postfixOps

trait Flying extends PlaneState {

  def airControl: ActorRef

  private var outOfKerozenCrash: Cancellable = null

  val flying = GameReceive {
    case Land(runway) =>
      reply(detail = Land(runway).toString)(newState = landing) {
        outOfKerozenCrash.cancel()

        import context.dispatcher
        context.system.scheduler.scheduleOnce(settings.aLandingDuration, self, Landed(runway))
      }

    case OutOfKerozen =>
      log.error("Plane {} is out of kerozen, it crashes", self.path.name)
      context stop self
  }

  def landing: Receive = {

    case Land(_) =>

    case this.Landed(runway) =>
      airControl ! HasLanded
      runway ! HasLanded

      updateStep("runway", s"On ${runway.path.name}")

      context become waitingToPark(runway)
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

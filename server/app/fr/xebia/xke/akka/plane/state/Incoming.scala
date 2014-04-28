package fr.xebia.xke.akka.plane.state

import akka.actor.{Cancellable, ActorRef}
import fr.xebia.xke.akka.airport.PlaneEvent
import fr.xebia.xke.akka.airport.PlaneEvent.{Incoming, HasLanded, OutOfKerozen}
import fr.xebia.xke.akka.airport.command.{Contact, Land}
import languageFeature.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.plane.Plane

private[plane] trait Incoming extends Plane with RadioCommunication {

  private var outOfKerozenCrashTask: Option[Cancellable] = None

  private var repeatIncomingTask: Option[Cancellable] = None

  def landing(airTrafficControl: ActorRef) = State("incoming", behavior = LoggingReceive {
    case msg@this.Landed(runway) =>
      runway ! HasLanded
      transitionTo(transition = () => {
        airTrafficControl ! HasLanded
      })(nextState = waitingToTaxi(airTrafficControl, runway))

  })

  def flying(airTrafficControl: ActorRef) = State("incoming", behavior = LoggingReceive {
    case Land(runway) =>

      replyWithRadio(() => {

        import context.dispatcher
        context.system.scheduler.scheduleOnce(settings.aLandingDuration, self, Landed(runway))

        transitionTo(transition = () => {
          for (task <- outOfKerozenCrashTask if !task.isCancelled) {
            task.cancel()
          }
          for (task <- repeatIncomingTask if !task.isCancelled) {
            task.cancel()
          }
        })(nextState = landing(airTrafficControl))
      })

    case OutOfKerozen =>
      terminateInError(s"Plane ${self.path.name} is out of kerozen, it crashes")
  })

  val idle = State("incoming", behavior = LoggingReceive {
    case Contact(airTrafficControl) =>
      import context.dispatcher
      outOfKerozenCrashTask = Some(context.system.scheduler.scheduleOnce(settings.outOfKerozenTimeout milliseconds, self, OutOfKerozen))

      transitionTo(transition = () => {
        repeatIncomingTask = Some(context.system.scheduler.schedule(0 milliseconds, 10 seconds, airTrafficControl, Incoming))

      })(nextState = flying(airTrafficControl))
  })

  def waitingToTaxi(airControl: ActorRef, runway: ActorRef): State

  private case class Landed(runway: ActorRef) extends PlaneEvent {
    override def toString = s"Landed on ${runway.path.name}"
  }

}

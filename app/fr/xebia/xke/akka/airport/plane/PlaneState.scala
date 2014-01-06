package fr.xebia.xke.akka.airport.plane

import akka.actor.{Cancellable, ActorLogging, ActorRef, Actor}
import fr.xebia.xke.akka.airport.Command.Ack
import fr.xebia.xke.akka.airport.{PlaneError, Command, PlaneEvent, Settings}
import controllers.PlaneStatus

trait PlaneState extends Actor with ActorLogging {

  protected def settings: Settings

  protected def game: ActorRef

  protected def eventStream = context.system.eventStream

  protected val plane = self

  private var status: PlaneStatus = PlaneStatus("", self.path.name, "", "")

  private var lastReply: Cancellable = null

  protected def replyTo(target: ActorRef)(command: => Unit) {
    if (settings.isRadioOk) {
      import context.dispatcher
      lastReply = context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          target ! Ack

          updateStatus("Ack")

          command
        }
      })
    }
  }

  protected def updateStatus(newStatus: String) {
    status = status.copy(detail = newStatus)
    publishState()
  }

  protected def updateError(error: String) {
    status = status.copy(error = error)
    publishState()
  }

  protected def updateStep(newStep: String, detail: String) {
    status = status.copy(step = newStep)
    publishState()
  }

  override def postStop() {
    if (lastReply != null && !lastReply.isCancelled) {
      lastReply.cancel()
    }
  }

  private def publishState() {
    eventStream publish status
  }

  type GameReceive = Receive

  object GameReceive {

    def apply(delegate: Receive): GameReceive =
      PartialFunction {
        publishPlaneStatus andThen (delegate orElse registerError)
      }

    private def publishPlaneStatus: (Any => Any) = {
      case error: PlaneError =>
        updateError(error.message)
        error

      case event: PlaneEvent =>
        updateStatus(event.toString)
        event

      case commandReceived: Command =>
        updateStatus(commandReceived.toString)
        commandReceived

      case other => other
    }

    private def registerError: Receive = {
      case msg =>
        context stop self
    }
  }

}
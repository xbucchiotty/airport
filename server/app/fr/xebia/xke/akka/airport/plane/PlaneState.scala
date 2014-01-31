package fr.xebia.xke.akka.airport.plane

import akka.actor.{Cancellable, ActorLogging, ActorRef, Actor}
import controllers.PlaneStatus
import fr.xebia.xke.akka.airport.command.{Command, Ack}
import fr.xebia.xke.akka.airport.{PlaneError, PlaneEvent, Settings}
import akka.event.EventStream

trait PlaneState extends Actor with ActorLogging {

  protected def settings: Settings

  protected def game: ActorRef

  def eventStream: EventStream

  protected val plane = self

  private var status: PlaneStatus = PlaneStatus("", self.path.name, "", "")

  private var lastReply: Cancellable = null

  protected def reply(detail: String)(newState: Receive)(action: => Unit) {
    if (settings.isRadioOk) {

      val lastSender = sender

      import context.dispatcher
      lastReply = context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          lastSender ! Ack

          updateStatus(s"Ack $detail")

          action
        }
      })

      context become newState
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

  protected def updateStep(newStep: String, newDetail: String) {
    status = status.copy(step = newStep, detail = newDetail)
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
        log.warning(s"${self.path.name} received a wrong order $msg}")
        context stop self
    }
  }

}
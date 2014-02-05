package fr.xebia.xke.akka.airport.plane.state

import akka.actor.ActorContext
import akka.actor.Actor.Receive
import akka.event.{LoggingAdapter, EventStream}
import fr.xebia.xke.akka.airport.plane.event.{StateChanged, ErrorHappened, DetailChanged}

private[plane] trait StateMachine {

  def context: ActorContext

  def eventStream: EventStream

  def log: LoggingAdapter

  def transitionTo(transition: Transition)(nextState: State) {
    transition()

    eventStream.publish(StateChanged(nextState.name))
    context become nextState.behavior
  }

  def done() {
    updateStatus("Well done!")
    eventStream.publish(StateChanged("done"))
    context stop context.self
  }

  def terminateInError(message: String) {
    log.debug(message)
    eventStream.publish(ErrorHappened(message))
    context stop context.self
  }

  private def updateStatus(detail: String) {
    eventStream.publish(DetailChanged(detail))
  }

  case class State(name: String, behavior: LoggingReceive)

  type LoggingReceive = Receive

  object LoggingReceive {

    def apply(delegate: Receive): LoggingReceive =
      PartialFunction {
        publishEvent andThen (delegate orElse registerError)
      }

    private def publishEvent: (Any => Any) = {
      case msg: Any =>
        updateStatus(msg.toString)
        msg
    }

    private def registerError: Receive = {
      case msg =>
        updateStatus(s"${context.self.path.name} received a wrong msg $msg}")
    }
  }

}
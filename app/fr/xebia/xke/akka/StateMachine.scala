package fr.xebia.xke.akka

import akka.actor.{ActorLogging, Actor}
import akka.event.EventStream
import fr.xebia.xke.akka.plane.event.ErrorHappened
import fr.xebia.xke.akka.plane.event.StateChanged
import fr.xebia.xke.akka.plane.event.DetailChanged
import language.postfixOps
import fr.xebia.xke.akka.airport.message.PlaneError

trait StateMachine {

  this: Actor with ActorLogging =>

  def eventStream: EventStream


  def transitionTo(transition: Transition)(nextState: State) {
    transition()

    setState(nextState)

  }

  def setState(state: State) {
    eventStream.publish(StateChanged(state.name))
    context become state.behavior
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
      case msg: PlaneError =>
        terminateInError(msg.toString())
        msg

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
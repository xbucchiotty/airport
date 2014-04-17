package fr.xebia.xke.akka.game

import akka.actor.{Cancellable, Props, Actor, ActorRef}
import scala.concurrent.duration._
import fr.xebia.xke.akka.airport.command.Ack

class OrderSender(to: ActorRef, msg: Any, ackMaxTimeout: FiniteDuration) extends Actor {

  var repeatOrder: Option[Cancellable] = _

  override def preStart(): Unit = {
    import context.dispatcher
    repeatOrder = Some(context.system.scheduler.schedule(0.millisecond, ackMaxTimeout, to, msg))
  }

  def receive: Receive = {
    case Ack =>
      for (task <- repeatOrder if !task.isCancelled) {
        task.cancel()
      }

      context stop self
  }
}

object OrderSender {

  def props(to: ActorRef, msg: Any, ackMaxTimeout: FiniteDuration) =
    Props(classOf[OrderSender], to, msg, ackMaxTimeout)
}

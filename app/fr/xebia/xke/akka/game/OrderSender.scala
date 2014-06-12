package fr.xebia.xke.akka.game

import akka.actor.{ActorLogging, Cancellable, Props, Actor, ActorRef}
import scala.concurrent.duration._
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack

class OrderSender(to: ActorRef, msg: Any, ackMaxTimeout: FiniteDuration) extends Actor with ActorLogging {

  var repeatOrder: Option[Cancellable] = _

  override def preStart(): Unit = {
    deliver()
  }

  def receive: Receive = {
    case Ack =>
      for (task <- repeatOrder if !task.isCancelled) {
        task.cancel()
      }

      log.debug(s"Ack of ${msg.getClass.getSimpleName} to ${to.path.name}")

      context stop self

    case OrderSender.Timeout =>
      log.debug(s"Timeout of ${msg.getClass.getSimpleName} to ${to.path.name}")
      deliver()
  }

  private def deliver() {
    to ! msg
    import context.dispatcher
    repeatOrder = Some(context.system.scheduler.scheduleOnce(ackMaxTimeout, self, OrderSender.Timeout))
    log.debug(s"Ensure delivery of ${msg.getClass.getSimpleName} to ${to.path.name}")
  }
}

object OrderSender {

  def props(to: ActorRef, msg: Any, ackMaxTimeout: FiniteDuration) =
    Props(classOf[OrderSender], to, msg, ackMaxTimeout)

  case object Timeout

}

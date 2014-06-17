package fr.xebia.xke.akka.player

import akka.actor.{ActorLogging, ActorRef, Actor}
import akka.actor.{ActorLogging, Actor, ActorRef}
import scala.concurrent.duration._
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack

class OrderSender(target: ActorRef, msg: Any, ackMaxTimeout: FiniteDuration) extends Actor with ActorLogging{

    override def preStart(){
        target ! msg
        import context.dispatcher
        context.system.scheduler.scheduleOnce(ackMaxTimeout, self, Timeout)
    }

    def receive: Receive = {

        case Ack =>

        log.debug(s"Ack of ${msg.getClass.getSimpleName} to ${target.path.name}")

        context stop self

    case Timeout =>

        log.debug(s"Timeout of ${msg.getClass.getSimpleName} to ${target.path.name}")
        target ! msg
        import context.dispatcher
        context.system.scheduler.scheduleOnce(ackMaxTimeout, self, Timeout)

    }
}

case object Timeout
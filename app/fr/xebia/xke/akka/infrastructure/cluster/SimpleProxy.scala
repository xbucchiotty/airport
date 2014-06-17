package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._
import fr.xebia.xke.akka.airport.message.command.Repeat
import scala.Some

class SimpleProxy(inbound: ActorRef, upperProxy: ActorRef) extends Actor with ActorLogging {

  var lastMessage: Option[(Any, ActorRef)] = None

  override def preStart() {
    context watch inbound

    log.debug(s"Creating an inbound proxy from <${self.path}> to ${inbound.path}")
  }

  def receive: Receive = {

    case SimpleProxy.Send(msg, outbound) =>
      log.debug(s"==> Sending <${msg.getClass.getSimpleName}> from <${inbound.path}> to <${outbound.path}>")

      outbound ! msg

      lastMessage = Some((msg, outbound))

    case Repeat =>
      lastMessage.foreach {
        case (msg, outbound) => {
          log.debug(s"==> Repeating <${msg.getClass.getSimpleName}> from <${inbound.path}> to <${outbound.path}>")
          sender() ! msg
        }
      }

    case Terminated(actor) if actor == inbound =>
      context stop self

    case any =>
      log.debug(s"<== Receiving <${any.getClass.getSimpleName}> from <${sender().path}> to <${inbound.path}>")
      upperProxy ! SimpleProxy.Reply(any, sender())
  }

}

object SimpleProxy {
  def props(target: ActorRef, upperProxy: ActorRef): Props = Props(classOf[SimpleProxy], target, upperProxy)

  case class Send(msg: Any, outbound: ActorRef)

  case class Reply(msg: Any, messageSender: ActorRef)

}
package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._
import fr.xebia.xke.akka.airport.command.Repeat
import scala.Some

class SimpleProxy(inbound: ActorRef, upperProxy: ActorRef) extends Actor with ActorLogging {

  var lastMessage: Option[(Any, ActorRef)] = None

  context watch inbound

  override def preStart() {
    log.debug(s"Creating an inbound proxy from <${self.path}> to ${sender().path}")
  }

  def receive: Receive = {

    case SimpleProxy.Send(msg, outbound) =>
      log.debug(s"==> Sending <$msg> from <${inbound.path}> to <${outbound.path}>")

      outbound ! msg

      lastMessage = Some((msg, outbound))

    case Repeat =>
      lastMessage.foreach {
        case (msg, outbound) => {
          log.debug(s"==> Repeating <$msg> from <${inbound.path}> to <${outbound.path}>")
          sender() ! msg
        }
      }

    case Terminated(actor) if actor == inbound =>
      context stop self

    case any =>
      log.debug(s"<== Receiving <$any> from <${sender().path}> to <${inbound.path}>")
      inbound.tell(any, upperProxy)
  }

}

object SimpleProxy {
  def props(target: ActorRef, upperProxy: ActorRef): Props = Props(classOf[SimpleProxy], target, upperProxy)

  case class Send(msg: Any, outbound: ActorRef)

}
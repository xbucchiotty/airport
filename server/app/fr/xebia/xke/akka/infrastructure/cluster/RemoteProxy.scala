package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._
import fr.xebia.xke.akka.infrastructure.cluster.RemoteProxy.{Register, Unregister}

class RemoteProxy(firstRemoteLookup: ActorSelection) extends Actor with ActorLogging {

  var inboundProxies = Map.empty[ActorRef, ActorRef]

  def receive = registered(firstRemoteLookup)

  def registered(target: ActorSelection): Receive = {
    case Register(newTarget) =>
      context become registered(newTarget)

    case Unregister =>
      context become unregistered(target)

    case any =>

      if (!inboundProxies.isDefinedAt(sender)) {
        log.debug(s"Creating an inbound proxy for ${sender.path.name}")
        inboundProxies += (sender -> context.actorOf(SimpleProxy.props(sender, self), sender.path.name))
      }

      log.debug(s"Sending $any to $target")
      target.tell(any, inboundProxies(sender))
  }

  def unregistered(lastTarget: ActorSelection): Receive = {
    case Register(newTarget) =>
    context become registered(newTarget)

    case any =>
      log.debug(s"Unable to deliver $any")
  }
}

object RemoteProxy {
  def props(remoteLookup: ActorSelection): Props = Props(classOf[RemoteProxy], remoteLookup)

  case object Unregister

  case class Register(newTarget: ActorSelection)

}
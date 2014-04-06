package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._

class RemoteProxy(remoteLookup: ActorSelection) extends Actor with ActorLogging {

  var inboundProxies = Map.empty[ActorRef, ActorRef]

  def receive: Receive = {
    case any =>

      if (!inboundProxies.isDefinedAt(sender)) {
        log.debug(s"Creating an inbound proxy for ${sender.path.name}")
        inboundProxies += (sender -> context.actorOf(SimpleProxy.props(sender, self), sender.path.name))
      }

      log.debug(s"Sending $any to $remoteLookup")
      remoteLookup.tell(any, inboundProxies(sender))
  }
}

object RemoteProxy {
  def props(remoteLookup: ActorSelection): Props = Props(classOf[RemoteProxy], remoteLookup)
}

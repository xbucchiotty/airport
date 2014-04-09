package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor._
import fr.xebia.xke.akka.infrastructure.cluster.RemoteProxy.{Register, Unregister}

class RemoteProxy(firstRemoteLookup: ActorRef) extends Actor with ActorLogging {

  var inboundProxies = Map.empty[ActorRef, ActorRef]

  log.debug(s"Creating an outbound proxy from <${self.path}> to ${firstRemoteLookup.path}")

  def receive = registered(firstRemoteLookup)

  def registered(target: ActorRef): Receive = {
    case Register(newTarget) =>
      context become registered(newTarget)

    case Unregister =>
      context become unregistered(target)

    case any =>

      if (!inboundProxies.isDefinedAt(sender())) {
        inboundProxies += (sender() -> context.actorOf(SimpleProxy.props(sender(), self), sender().path.name))
      }

      inboundProxies(sender()) ! SimpleProxy.Send(any, target)
  }

  def unregistered(lastTarget: ActorRef): Receive = {
    case Register(newTarget) =>
      context become registered(newTarget)

    case any =>
      log.debug(s"Unable to deliver <$any> to <${self.path.name}>}")
  }
}

object RemoteProxy {
  def props(remoteLookup: ActorRef): Props = Props(classOf[RemoteProxy], remoteLookup)

  case object Unregister

  case class Register(newTarget: ActorRef)

}
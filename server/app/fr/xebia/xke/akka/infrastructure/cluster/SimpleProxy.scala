package fr.xebia.xke.akka.infrastructure.cluster

import akka.actor.{Props, ActorLogging, Actor, ActorRef}

class SimpleProxy(target: ActorRef, upperProxy: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {

    case any =>
      log.debug(s"Sending $any to ${target.path.name}")
      target.tell(any, upperProxy)
  }

}

object SimpleProxy {
  def props(target: ActorRef, upperProxy: ActorRef): Props = Props(classOf[SimpleProxy], target, upperProxy)
}
package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Terminated, Props, ActorRef, Actor}

class Game extends Actor with ActorLogging {

  var airTrafficControl: ActorRef = null

  def this(airControl: ActorRef) = {
    this()
    this.airTrafficControl = airControl
  }

  override def preStart() {
    if (airTrafficControl == null)
      airTrafficControl = context.actorOf(Props[Runway])

    context watch airTrafficControl
  }

  def receive: Receive = {
    case Terminated(ref) if ref == airTrafficControl =>
      log.info("Game terminates because of the air traffic")
      context stop self

  }

}

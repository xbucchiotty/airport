package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Terminated, Props, ActorRef, Actor}

class Game extends Actor with ActorLogging {

  var runway: ActorRef = null

  def this(runway: ActorRef) = {
    this()
    this.runway = runway
  }

  override def preStart() {
    if (runway == null)
      runway = context.actorOf(Props[Runway])

    context watch runway
  }

  def receive: Receive = {
    case Terminated(ref) if ref == runway =>
      log.info("Game terminates because of the runway")
      context stop self


    case m => s"Whoops $m"
  }

}

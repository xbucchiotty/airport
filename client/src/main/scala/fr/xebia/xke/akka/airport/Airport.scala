package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, Actor}

class Airport extends Actor {

  var groundControl: ActorRef = _
  var airTrafficControl: ActorRef = _

  override def preStart() {
    groundControl = context.actorOf(Props[GroundControl], "groundControl")

    airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl), "airTrafficControl")

  }

  def receive: Receive = {
    case _ =>
  }
}

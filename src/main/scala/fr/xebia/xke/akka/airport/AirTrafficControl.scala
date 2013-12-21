package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, Actor}
import fr.xebia.xke.akka.airport.Command.{Contact, Land}
import fr.xebia.xke.akka.airport.Event.{Incoming, Landed}

class AirTrafficControl(groundControl: ActorRef) extends Actor {

  private val runway = context.actorOf(Props(classOf[Runway], self), "runway")

  def receive: Receive = {

    case Incoming =>
      sender ! Land(runway)

    case Landed(plane, _) =>
      plane ! Contact(groundControl)
  }

}

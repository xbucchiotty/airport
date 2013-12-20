package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, Actor}
import fr.xebia.xke.akka.airport.Command.{Incoming, Contact, Land}
import fr.xebia.xke.akka.airport.Event.Landed

class AirTrafficControl(groundControl: ActorRef) extends Actor {

  private val runway = context.actorOf(Props(classOf[Runway], self), "runway")

  def receive: Receive = {

    case Incoming =>
      sender ! Land(runway)

    case Landed(plane) =>
      plane ! Contact(groundControl)
  }

}

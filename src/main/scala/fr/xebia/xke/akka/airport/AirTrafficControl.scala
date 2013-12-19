package fr.xebia.xke.akka.airport

import akka.actor.{Props, Actor}
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.Incoming

class AirTrafficControl extends Actor {

  private val runway = context.actorOf(Props(classOf[Runway], self), "runway")

  def monitoring: Receive = {

    case Incoming =>
      sender ! Land(runway)

  }

  def receive: Receive = monitoring

}

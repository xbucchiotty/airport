package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import fr.xebia.xke.akka.airport.Command.{Contact, Land}
import fr.xebia.xke.akka.airport.Event.{HasLeft, Incoming, HasLanded}

class AirTrafficControl(groundControl: ActorRef) extends Actor with ActorLogging {

  private val runway = context.actorOf(Props(classOf[Runway], self), "runway")

  def receive: Receive = {

    case Incoming =>
      sender ! Land(runway)

    case HasLanded(plane, _) =>
      plane ! Contact(groundControl)

    case HasLeft(_, _) =>
      log.warning("TODO IMPLEMENTS ME (air traffic control notified of a plane has left a runway)")

  }

}

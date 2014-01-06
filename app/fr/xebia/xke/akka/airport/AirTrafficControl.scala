package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import fr.xebia.xke.akka.airport.Command.{Contact, Land}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, HasLanded}

class AirTrafficControl(groundControl: ActorRef, runway: Seq[ActorRef]) extends Actor with ActorLogging {

  def receive: Receive = {

    case Incoming =>
      sender ! Land(runway.head)

    case HasLanded =>
      sender ! Contact(groundControl)

    case HasLeft =>

  }

}

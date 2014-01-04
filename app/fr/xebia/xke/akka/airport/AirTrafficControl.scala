package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, ActorRef, Props, Actor}
import fr.xebia.xke.akka.airport.Command.{Contact, Land}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, HasLanded}

class AirTrafficControl(groundControl: ActorRef, runway: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {

    case Incoming =>
      log.info(s"plane ${sender.path.name} requests to land!")
      sender ! Land(runway)

    case HasLanded =>
      log.info(s"plane ${sender.path.name} has landed!")
      sender ! Contact(groundControl)

    case HasLeft =>
      log.info(s"plane ${sender.path.name} has left!")
      //sender ! "Bye"

  }

}

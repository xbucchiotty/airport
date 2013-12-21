package fr.xebia.xke.akka.airport

import akka.actor.{Props, Actor}
import fr.xebia.xke.akka.airport.Event.Incoming
import fr.xebia.xke.akka.airport.Command.Park

class GroundControl extends Actor {

  private val taxiway = context.actorOf(Props(classOf[Taxiway], 1, self), "taxiway")
  private val gate = context.actorOf(Props[Gate], "gate")

  def receive: Receive = {
    case Incoming =>
      sender ! Park(taxiway, gate)
  }
}

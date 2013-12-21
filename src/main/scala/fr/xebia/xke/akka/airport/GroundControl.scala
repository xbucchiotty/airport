package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Props, Actor}
import fr.xebia.xke.akka.airport.Event.{Entered, Incoming}
import fr.xebia.xke.akka.airport.Command.Park

class GroundControl extends Actor with ActorLogging {

  private val taxiway = context.actorOf(Props(classOf[Taxiway], 1, self), "taxiway")
  private val gate = context.actorOf(Props[Gate], "gate")

  def receive: Receive = {
    case Incoming =>
      sender ! Park(taxiway, gate)

    case Entered(plane, aTaxiway) =>
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has entered a taxiway)")

    case Left(_) =>
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has left a taxiway)")
  }
}

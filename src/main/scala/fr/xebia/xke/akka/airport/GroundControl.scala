package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Props, Actor}
import fr.xebia.xke.akka.airport.Event.{HasEntered, HasLeft, Incoming}
import fr.xebia.xke.akka.airport.Command.TaxiAndPark

class GroundControl extends Actor with ActorLogging {

  private val taxiway = context.actorOf(Props(classOf[Taxiway], 1, self), "taxiway")
  private val gate = context.actorOf(Props(classOf[Gate], self), "gate")

  def receive: Receive = {
    case Incoming =>
      sender ! TaxiAndPark(taxiway, gate)

    case HasEntered(plane, aTaxiway) =>
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has entered a taxiway)")

    case HasLeft(plane, aTaxiway) =>
      log.warning("TODO IMPLEMENTS ME (ground control notified of a plane has left a taxiway)")
  }
}

package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, ActorRef, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, HasLanded}
import fr.xebia.xke.akka.airport.command._
import akka.persistence.EventsourcedProcessor
import scala.collection.immutable.Queue
import scala.concurrent.duration._

class AirTrafficControl extends Actor with ActorLogging {

  var groundControl: ActorRef = null
  var ackMaxTimeout: Int = _
  var runways = Set.empty[ActorRef]

  log.info("ATC created")

  def receive: Receive = {

    case Incoming =>
      val plane = sender()

    case HasLanded =>
      val plane = sender()

    case HasLeft =>
      val plane = sender()


    //Initialization
    case InitAirTrafficControl(_groundControl, _runways, _ackMaxTimeout) =>
      sender ! AirTrafficControlReady

      log.info("ATC ready")

      this.groundControl = _groundControl
      this.runways = _runways
      this.ackMaxTimeout = _ackMaxTimeout
  }

}
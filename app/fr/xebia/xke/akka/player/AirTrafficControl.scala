package fr.xebia.xke.akka.player

import akka.actor._
import akka.persistence.EventsourcedProcessor
import fr.xebia.xke.akka.airport.message.{AirTrafficControlReady, InitAirTrafficControl, ChaosMonkey}
import fr.xebia.xke.akka.airport.message.PlaneEvent.{HasLeft, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.message.command.{Contact, Land}

import scala.collection.immutable.Queue

class AirTrafficControl extends Actor with ActorLogging {

  var groundControl: ActorRef = null
  var ackMaxTimeout: Int = _
  var runways = Set.empty[ActorRef]

  def receive: Receive = {

    case Incoming =>
      val plane = sender()

    case HasLanded =>
      val plane = sender()

    case HasLeft =>
      val plane = sender()


    case ChaosMonkey =>
      log.error("Oh no, a chaos monkey!!!!")
      throw new ChaosMonkeyException

    //Initialization
    case InitAirTrafficControl(_groundControl, _runways, _ackMaxTimeout) =>

      this.groundControl = _groundControl
      this.runways = _runways
      this.ackMaxTimeout = _ackMaxTimeout

      sender() ! AirTrafficControlReady
  }

}

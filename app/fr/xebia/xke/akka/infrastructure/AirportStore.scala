package fr.xebia.xke.akka.infrastructure

import akka.actor.{ActorLogging, Props, Actor}
import fr.xebia.xke.akka.infrastructure.AirportStore._
import fr.xebia.xke.akka.airport.{AirportCode, Airport}

class AirportStore(airports: List[Airport]) extends Actor with ActorLogging {

  var availableAirports: List[Airport] = airports

  var boundAirports: List[Airport] = Nil

  def receive: Receive = {
    case Register =>
      register()

    case IsRegistered(airportCode) =>
      sender() ! boundAirports.find(_.code == airportCode)
  }

  def register() {
    val (airport :: tail) = availableAirports
    availableAirports = tail

    boundAirports = boundAirports :+ airport

    sender() ! Registered(airport)

    log.info(s"Registered airport ${airport.code}")
  }
}

object AirportStore {

  def props(airports: List[Airport]): Props = Props(classOf[AirportStore], airports)

  case class Registered(airport: Airport)

  case object Register

  case class IsRegistered(airportCode: AirportCode)

}

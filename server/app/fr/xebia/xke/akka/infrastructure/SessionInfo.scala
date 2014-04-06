package fr.xebia.xke.akka.infrastructure

import fr.xebia.xke.akka.airport.{AirportCode, Airport}


case class SessionInfo(sessionId: SessionId, airport: Airport) {

  def airportCode: AirportCode = airport.code
}
package fr.xebia.xke.akka.infrastructure

import fr.xebia.xke.akka.airport.{AirportCode, Airport}


case class UserInfo(userId: SessionId, airport: Airport) {
  def airportCode: AirportCode = airport.code
}
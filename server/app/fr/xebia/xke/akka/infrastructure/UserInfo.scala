package fr.xebia.xke.akka.infrastructure

import fr.xebia.xke.akka.airport.{AirportCode, Airport}


case class UserInfo(userId: TeamMail, airport: Airport) {
  def airportCode: AirportCode = airport.code
}
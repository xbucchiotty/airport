package fr.xebia.xke.akka.airport.game

import fr.xebia.xke.akka.airport.Airport
import akka.actor.Address

case class UserInfo(userId: TeamMail, airport: Airport) {
  def airportCode = airport.code
}
package fr.xebia.xke.akka.infrastructure


case class UserInfo(userId: TeamMail, airport: Airport) {
  def airportCode: AirportCode = airport.code
}
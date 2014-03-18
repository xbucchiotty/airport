package fr.xebia.xke.akka.airport

object Airport {

  lazy val airports: Set[Airport] = {
    val routesFrom = Route.routesFrom
    val routesTo = Route.routesTo

    scala.io.Source.fromInputStream(classOf[Airport].getClassLoader.getResourceAsStream("data/airports.dat"))
      .getLines()
      .filter(_.nonEmpty)
      .map(_.replaceAll("\"", ""))
      .map(line => line.split(','))
      .map(data => {

      val airportCode = data(4)

      val departures = routesFrom.get(airportCode).getOrElse(Nil)
      val arrivals = routesTo.get(airportCode).getOrElse(Nil)

      Airport(
        city = data(2),
        code = airportCode,
        latitude = data(6),
        longitude = data(7),
        departures = departures.toSet,
        arrivals = arrivals.toSet)

    }).toSet
  }

  lazy val top100: Set[Airport] = airports
    .map(airport => (airport, airport.departures.size + airport.arrivals.size))
    .toList
    .sortBy(_._2)
    .map(_._1)
    .reverse
    .take(100)
    .toSet

  def fromCode(code: String): Option[Airport] = airports.find(_.code == code)
}

case class Airport(city: String, code: String, latitude: String, longitude: String, departures: Set[Route] = Set.empty, arrivals: Set[Route] = Set.empty) {

  override def toString = s"Airport($city $code, ${departures.size} departures, ${arrivals.size} arrivals)"
}

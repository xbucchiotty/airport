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

      val airportCode = AirportCode(data(4))

      val departures = routesFrom.get(airportCode).getOrElse(Nil)
      val arrivals = routesTo.get(airportCode).getOrElse(Nil)

      Airport(
        city = data(2),
        code = airportCode,
        latitude = data(6),
        longitude = data(7),
        departures = departures.toList,
        arrivals = arrivals.toList)

    }).toSet
  }

  lazy val top100: List[Airport] = airports
    .map(airport => (airport, airport.departures.size + airport.arrivals.size))
    .toList
    .sortBy(byRouteSize)
    .map(toAirport)
    .reverse
    .take(100)
    .toList


  def toAirport: ((Airport, Int)) => Airport = {
    case (airport, routeSize) => airport
  }

  def byRouteSize: ((Airport, Int)) => Int = {
    case (airport, routeSize) => routeSize
  }

  def fromCode(code: AirportCode): Option[Airport] = airports.find(_.code == code)
}

case class Airport(city: String, code: AirportCode, latitude: String, longitude: String, departures: List[Route] = Nil, arrivals: List[Route] = Nil) {

  override def toString = s"Airport($city $code, ${departures.size} departures, ${arrivals.size} arrivals)"
}
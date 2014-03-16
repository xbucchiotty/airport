package fr.xebia.xke.akka.airport


object Airport {
  lazy val airports: Set[Airport] = scala.io.Source.fromInputStream(ClassLoader.getSystemResourceAsStream("data/airports.dat"))
    .getLines()
    .filter(_.nonEmpty)
    .map(_.replaceAll("\"", ""))
    .map(line => line.split(','))
    .map(data => Airport(city = data(2), code = data(4), latitude = data(6), longitude = data(7)))
    .toSet

  def fromCode(code: String): Option[Airport] = airports.find(_.code == code)
}

case class Airport(city: String, code: String, latitude: String, longitude: String)

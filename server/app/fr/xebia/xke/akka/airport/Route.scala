package fr.xebia.xke.akka.airport

case class Route(from: String, to: String, stops: Int, airline: String) {
  override def toString = s"$airline: $from -> $to"
}

object Route {

  def routesFrom: Map[String, Iterable[Route]] = routes
    .groupBy(_.from)
    .mapValues(_.toList)

  def routesTo: Map[String, Iterable[Route]] = routes
    .groupBy(_.to)
    .mapValues(_.toList)

  private def routes = scala.io.Source.fromInputStream(classOf[Route].getClassLoader.getResourceAsStream("data/routes.dat"))
    .getLines()
    .toStream
    .filter(_.nonEmpty)
    .map(line => line.split(','))
    .map(data => Route(from = data(2), to = data(4), stops = data(7).toInt, airline = data(0)))
}

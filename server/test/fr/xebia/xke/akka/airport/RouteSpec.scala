package fr.xebia.xke.akka.airport

import org.scalatest.{ShouldMatchers, FunSpec}

class RouteSpec extends FunSpec with ShouldMatchers {

  describe("Routes") {
    it("should be able to find route CDG -> HKG by AF with 0 stop") {
      val result: Option[Route] = Route.find("CDG", "HKG")

      result should be(defined)

      result.map(route => {
        route.from should equal("CDG")
        route.to should equal("HKG")
        route.stops should equal(0)
        route.airline should equal("AF")
      })
    }
  }
}


case class Route(from: String, to: String, stops: Int, airline: String)

object Route {

  lazy val routes: Set[Route] = scala.io.Source.fromInputStream(ClassLoader.getSystemResourceAsStream("data/routes.dat"))
    .getLines()
    .filter(_.nonEmpty)
    .map(line => line.split(','))
    .map(data => Route(from = data(2), to = data(4), stops = data(7).toInt, airline = data(0)))
    .toSet

  def find(from: String, to: String): Option[Route] = {
    routes.find(route => route.from == from && route.to == to)
  }
}

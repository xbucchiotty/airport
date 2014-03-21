package fr.xebia.xke.akka.infrastructure

import org.scalatest.{ShouldMatchers, FunSpec}

class RouteSpec extends FunSpec with ShouldMatchers {

  describe("Routes") {

    it("should be able to find routes from CDG with a route to HKG by AF with 0 stop") {
      val routesFromCDG = Route.routesFrom(AirportCode("CDG"))

      routesFromCDG should not(be(empty))
      routesFromCDG.exists(_.from != AirportCode("CDG")) should equal(false)

      routesFromCDG should contain(Route(from = AirportCode("CDG"), to = AirportCode("HKG"), stops = 0, airline = "AF"))

    }

    it("should be able to find routes to CDG with a route from JFK by AF with 0 stop") {
      val routesFromCDG = Route.routesTo(AirportCode("CDG"))

      routesFromCDG should not(be(empty))
      routesFromCDG.exists(_.to != AirportCode("CDG")) should equal(false)

      routesFromCDG should contain(Route(from = AirportCode("JFK"), to = AirportCode("CDG"), stops = 0, airline = "AF"))

    }

  }
}
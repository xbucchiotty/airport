package fr.xebia.xke.akka.airport

import org.scalatest.{ShouldMatchers, FunSpec}

class RouteSpec extends FunSpec with ShouldMatchers {

  describe("Routes") {

    it("should be able to find routes from CDG with a route to HKG by AF with 0 stop") {
      val routesFromCDG = Route.routesFrom("CDG")

      routesFromCDG should not(be(empty))
      routesFromCDG.exists(_.from != "CDG") should equal(false)

      routesFromCDG should contain(Route(from = "CDG", to = "HKG", stops = 0, airline = "AF"))

    }

    it("should be able to find routes to CDG with a route from JFK by AF with 0 stop") {
      val routesFromCDG = Route.routesTo("CDG")

      routesFromCDG should not(be(empty))
      routesFromCDG.exists(_.to != "CDG") should equal(false)

      routesFromCDG should contain(Route(from = "JFK", to = "CDG", stops = 0, airline = "AF"))

    }

  }
}
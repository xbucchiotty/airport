package fr.xebia.xke.akka.airport

import org.scalatest.{ShouldMatchers, FunSpec}

class AirportSpec extends FunSpec with ShouldMatchers {

  describe("Airports") {

    it("should be able to find ORY in airports data") {
      val result = Airport.fromCode("CDG")

      result should be(defined)

      result.map(orly => {
        orly.city should equal("Paris")
        orly.code should equal("CDG")
        orly.latitude should equal("49.012779")
        orly.longitude should equal("2.55")

        orly.departures should contain(Route("CDG", "HKG", 0, "AF"))
        orly.arrivals should contain(Route("JFK", "CDG", 0, "AF"))
      })
    }

    it("should find top 100 of biggest airport by routes") {
      val airports = Airport.top100

      airports.foreach(println)
    }
  }

}







package fr.xebia.xke.akka.airport

import org.scalatest.{ShouldMatchers, FunSpec}

class AirportsSpec extends FunSpec with ShouldMatchers {

  describe("Airports") {

    it("should be able to find ORY in airports data") {
      val result = Airport.fromCode("ORY")

      result should be(defined)

      result.map(orly => {
        orly.city should equal("Paris")
        orly.code should equal("ORY")
        orly.latitude should equal("48.725278")
        orly.longitude should equal("2.359444")
      })
    }
  }

}







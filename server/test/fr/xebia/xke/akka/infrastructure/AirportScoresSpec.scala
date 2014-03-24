package fr.xebia.xke.akka.infrastructure

import org.scalatest.{ShouldMatchers, FunSpec}

class AirportScoresSpec extends FunSpec with ShouldMatchers {

  describe("Airports score") {

    it("should be serialized to JSON in the expected format") {
      val scores = AirportScores(Seq(AirportScore(AirportCode("LHR"), 42.1, 2.32, 28)))

      val json = AirportScores.jsonWriter.writes(scores)

      json.toString() should equal( """[42.1,2.32,28.0]""")
    }
  }
}

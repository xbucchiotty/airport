package fr.xebia.xke.akka.infrastructure

import play.api.libs.json.{JsNumber, JsArray}

case class AirportScore(airportCode: AirportCode, latitude: Double, longitude: Double, score: Double)

object AirportScore {

  def toJson(o: AirportScore): JsArray =
    JsArray(Seq(JsNumber(o.latitude), JsNumber(o.longitude), JsNumber(o.score))

    )
}

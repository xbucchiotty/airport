package fr.xebia.xke.akka.infrastructure

import play.api.libs.json.{JsArray, Writes}

case class AirportScores(scores: Seq[AirportScore]) extends AnyVal

object AirportScores {

  implicit val jsonWriter: Writes[AirportScores] = new Writes[AirportScores] {

    def writes(o: AirportScores): JsArray = {
      o.scores.foldLeft(JsArray(Seq.empty))((json, score) => json ++ AirportScore.toJson(score))
    }
  }
}

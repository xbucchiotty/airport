package fr.xebia.xke.akka.airport

import play.api.mvc.PathBindable

case class AirportCode(code: String) extends AnyVal {
  override def toString = code
}

object AirportCode {
  implicit val pathBindable = new PathBindable[AirportCode] {
    override def unbind(key: String, airportCode: AirportCode): String = airportCode.toString

    override def bind(key: String, pathParam: String): Either[String, AirportCode] = Right(AirportCode(pathParam))
  }

}
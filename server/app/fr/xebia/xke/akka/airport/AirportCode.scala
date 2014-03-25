package fr.xebia.xke.akka.airport

case class AirportCode(code: String) extends AnyVal {
  override def toString = code
}

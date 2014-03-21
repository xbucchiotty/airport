package fr.xebia.xke.akka.infrastructure

case class AirportCode(code: String) extends AnyVal {
  override def toString = code
}

package fr.xebia.xke.akka.infrastructure

case class SessionId(value: String) extends AnyVal {
  override def toString = value
}
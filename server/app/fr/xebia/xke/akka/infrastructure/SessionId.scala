package fr.xebia.xke.akka.infrastructure

import java.util.UUID
import play.api.mvc.PathBindable
import scala.util.{Failure, Success, Try}

case class SessionId(value: String) extends AnyVal {
  override def toString = value.toString
}

object SessionId {

  def apply(): SessionId = new SessionId(UUID.randomUUID().toString.split("-").head)

  implicit val pathBindable = new PathBindable[SessionId] {
    override def unbind(key: String, sessionId: SessionId): String = sessionId.toString

    override def bind(key: String, pathParam: String): Either[String, SessionId] = Right(SessionId(pathParam))
  }
}
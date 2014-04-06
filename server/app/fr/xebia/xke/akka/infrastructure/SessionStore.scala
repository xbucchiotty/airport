package fr.xebia.xke.akka.infrastructure

import akka.actor.{ActorLogging, Props, Actor}
import fr.xebia.xke.akka.infrastructure.SessionStore._
import scala.Some
import fr.xebia.xke.akka.airport.{AirportCode, Airport}

class SessionStore(airports: List[Airport]) extends Actor with ActorLogging {

  var associationsBySessionId: Map[SessionId, SessionInfo] = _
  var availableAirports: List[Airport] = airports

  override def preStart() {
    associationsBySessionId = Map.empty
  }

  def receive: Receive = {
    case Register(session) if associationsBySessionId.contains(session) =>
      log.warning(s"Try to register twice session $session")
      sender ! RegisterError("SessionId already registered")

    case Register(sessionId) =>
      registerUser(sessionId)

    case Ask(sessionId) =>
      sender ! associationsBySessionId.get(sessionId)

    case AskForAirport(airportCode) =>
      sender ! associationsBySessionId.values.find(_.airportCode == airportCode)
  }

  def registerUser(session: SessionId) {
    val (airport :: tail) = availableAirports
    availableAirports = tail

    val info = SessionInfo(session, airport)
    associationsBySessionId += (session -> info)

    sender ! Registered(info)

    log.info(s"Session <$session> is registered to airport ${airport.code}")
  }
}

object SessionStore {

  def props(airports: List[Airport]): Props = Props(classOf[SessionStore], airports)

  case class Ask(sessionId: SessionId)

  case class AskForAirport(airportCode: AirportCode)

  case class RegisterError(message: String)

  case class Registered(userInfo: SessionInfo)

  case class Register(sessionId: SessionId)

}

package fr.xebia.xke.akka.infrastructure

import akka.actor.{ActorLogging, Props, Actor}
import fr.xebia.xke.akka.infrastructure.UserStore._
import scala.Some
import fr.xebia.xke.akka.airport.{AirportCode, Airport}

class UserStore(airports: List[Airport]) extends Actor with ActorLogging {

  var associationsByUserId: Map[SessionId, UserInfo] = _
  var availableAirports: List[Airport] = airports

  override def preStart() {
    associationsByUserId = Map.empty
  }

  def receive: Receive = {
    case Register(user) if associationsByUserId.contains(user) =>
      log.warning(s"Try to register twice user $user")
      sender ! RegisterError("Email already registered")

    case Register(user) =>
      registerUser(user)

    case Ask(user) if associationsByUserId.isDefinedAt(user) =>
      sender ! Some(associationsByUserId(user))

    case Ask(user) =>
      sender ! None

    case AskForAirport(airportCode) =>
      sender ! associationsByUserId.values.find(_.airportCode == airportCode)
  }

  def registerUser(user: SessionId) {
    val (airport :: tail) = availableAirports
    availableAirports = tail

    val info = UserInfo(user, airport)
    associationsByUserId += (user -> info)

    sender ! Registered(info)

    log.info(s"User <$user> is registered to airport ${airport.code}")
  }
}

object UserStore {

  def props(airports: List[Airport]): Props = Props(classOf[UserStore], airports)

  case class Ask(team: SessionId)

  case class AskForAirport(airportCode: AirportCode)

  case class RegisterError(message: String)

  case class Registered(userInfo: UserInfo)

  case class Register(userId: SessionId)

}

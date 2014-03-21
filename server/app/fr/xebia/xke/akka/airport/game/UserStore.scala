package fr.xebia.xke.akka.airport.game

import akka.actor.{ActorLogging, Props, Actor}
import fr.xebia.xke.akka.airport.Airport
import fr.xebia.xke.akka.airport.game.UserStore._
import fr.xebia.xke.akka.airport.game.UserStore.Ask
import fr.xebia.xke.akka.airport.game.UserStore.RegisterError
import fr.xebia.xke.akka.airport.game.UserStore.Register
import fr.xebia.xke.akka.airport.game.UserStore.Registered
import scala.Some

class UserStore extends Actor with ActorLogging {

  var associationsByUserId: Map[TeamMail, UserInfo] = _
  var availableAirports: List[Airport] = _

  override def preStart() {
    availableAirports = Airport.top100
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

  def registerUser(user: TeamMail) {
    val (airport :: tail) = availableAirports
    availableAirports = tail

    val info = UserInfo(user, airport)
    associationsByUserId += (user -> info)

    sender ! Registered(info)

    log.info(s"User <$user> is registered to airport ${airport.code}")
  }
}

object UserStore {

  def props(): Props = Props[UserStore]

  case class Ask(team: TeamMail)

  case class AskForAirport(airportCode: String)

  case class RegisterError(message: String)

  case class Registered(userInfo: UserInfo)

  case class Register(userId: TeamMail)

}

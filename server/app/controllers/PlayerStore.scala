package controllers

import akka.actor._
import fr.xebia.xke.akka.airport.{PlayerUp, Airport}
import controllers.PlayerStore._
import scala.util.Random
import controllers.PlayerStore.BindError
import controllers.PlayerStore.Register
import controllers.PlayerStore.BindActorSystem
import scala.Some
import controllers.PlayerStore.BoundActorSystem

class PlayerStore(gameStore: ActorRef) extends Actor with ActorLogging {

  var availableAirports: Set[Airport] = _

  var associationsBySystem: Map[Address, Airport] = _
  var associationsByUserId: Map[TeamMail, UserInfo] = _

  override def preStart() {
    availableAirports = Airport.top100

    associationsBySystem = Map.empty
    associationsByUserId = Map.empty
  }

  def receive: Receive = {

    case BindActorSystem(address, roles) if associationsBySystem.contains(address) =>
      sender ! BoundActorSystem(address, associationsBySystem(address))

    case BindActorSystem(address, roles) =>
      bindActorSystem(roles, address)

    case Register(user) if associationsByUserId.contains(user) =>
      log.warning(s"Try to register twice user $user")
      sender ! RegisterError("Email already registered")

    case Register(user) =>
      registerUser(user)

    case Ask(user) if associationsByUserId.isDefinedAt(user) =>
      sender ! Some(associationsByUserId(user))

    case Ask(user) =>
      sender ! None
  }

  def registerUser(user: controllers.TeamMail) {
    val airport = availableAirports.toList(Random.nextInt(availableAirports.size))
    availableAirports -= airport

    val info = UserInfo(user, airport)
    associationsByUserId += (user -> info)

    sender ! Registered(info)

    log.info(s"User <$user> is registered to airport ${airport.code}")
  }

  def bindActorSystem(roles: Set[String], address: Address) {
    val firstMatchingAirport: Option[Airport] = roles.collectFirst {
      case role: String if associationsByUserId.exists(_._2.airportCode == role) =>
        associationsByUserId.values.find(_.airportCode == role).map(_.airport).get
    }

    firstMatchingAirport match {
      case None =>
        log.warning(s"Impossible to bind an airport in $roles without any user registered before")
        sender ! BindError(s"Impossible to bind an airport in $roles without any user registered before")

      case Some(airport) =>
        if (associationsBySystem.exists(_._2 == airport)) {
          log.warning(s"System <$address> tried to bind airport <${airport.code}>, but it's already bound to <${associationsBySystem.find(_._2 == airport).map(_._1)}>")

          sender ! BindError(s"Airport ${airport.code} is already bound to a system")

        } else {
          val association = associationsByUserId.find(_._2.airport == airport)

          association match {
            case None =>

              log.warning(s"Receive registration of system <$address> to airport <${airport.code}> but no user is registered to it")
              sender ! BindError(s"Impossible to bind airport ${airport.code} without any user registered before")

            case Some((userId, userInfo)) =>

              log.info(s"System <$address> is bound to airport <${airport.code}> for user <$userId>")

              associationsByUserId = associationsByUserId.updated(userId, userInfo.copy(playerSystemAddress = Some(address)))
              associationsBySystem += (address -> airport)

              sender ! BoundActorSystem(address, airport)

              gameStore ! PlayerUp(userId, address)
          }
        }

    }
  }
}

object PlayerStore {

  def props(gameStore: ActorRef): Props = Props(classOf[PlayerStore], gameStore)

  case class BindActorSystem(address: Address, roles: Set[String])

  case class BoundActorSystem(address: Address, airport: Airport)

  case class BindError(message: String)

  case class Register(userId: TeamMail)

  case class Registered(userInfo: UserInfo)

  case class RegisterError(message: String)

  case class Ask(team: TeamMail)

}

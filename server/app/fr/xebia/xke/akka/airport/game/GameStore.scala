package fr.xebia.xke.akka.airport.game

import akka.actor._
import fr.xebia.xke.akka.airport.plane.Plane
import fr.xebia.xke.akka.airport._
import akka.event.EventStream
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.airport.game.GameStore._
import fr.xebia.xke.akka.airport.game.GameStore.Ask
import fr.xebia.xke.akka.airport.InitGame
import fr.xebia.xke.akka.airport.PlayerUp
import fr.xebia.xke.akka.airport.game.GameStore.NewGame
import fr.xebia.xke.akka.airport.game.GameStore.StartGame

class GameStore extends Actor with ActorLogging {

  var gameContexts: Map[TeamMail, GameContext] = _
  var gameCounter: Int = _

  implicit val timeout = Timeout(1 second)


  override def preStart() {
    gameContexts = Map.empty
    gameCounter = 0
  }

  def receive: Receive = {

    case NewGame(userInfo, settings, planeType) =>
      newGame(userInfo, settings, planeType)

    case StartGame(userInfo) if gameContexts.contains(userInfo.userId) =>
      startGame(userInfo)

    case event@PlayerUp(userId, address) if gameContexts.contains(userId) =>
      gameContexts(userId).publish(event)

    case event@PlayerDown(userId, address) if gameContexts.contains(userId) =>
      gameContexts(userId).publish(event)

    case Ask(userId) =>
      sender ! gameContexts.get(userId)
  }

  def newGame(userInfo: UserInfo, settings: Settings, planeType: Class[_ <: Plane]) {
    for (gameContext <- gameContexts.get(userInfo.userId)) {

      gameContext.stop(context.system)

      gameContexts -= userInfo.userId
    }

    val sessionId = s"game-session-${userInfo.airportCode}-$gameCounter"

    val gameContext = GameContext.create(sessionId, settings, planeType)(context)
    gameCounter += 1

    gameContexts += (userInfo.userId -> gameContext)

    log.info(s"Create a new game for <${userInfo.userId}>")

    sender ! GameCreated(gameContext)
  }

  def startGame(user: UserInfo) {
    for (gameContext <- gameContexts.get(user.userId)) {
      log.info(s"Start the game for <${user.userId}>, session = <${gameContext.game.path.name}>")

      val airTrafficControl = context.actorSelection(s"/user/airports/${user.airportCode}/airTrafficControl")
      val groundControl = context.actorSelection(s"/user/airports/${user.airportCode}/groundControl")

      gameContext.init(airTrafficControl, groundControl)

      sender ! GameStarted
    }
  }
}

object GameStore {

  def props(): Props = Props[GameStore]

  case class NewGame(user: UserInfo, settings: Settings, planeType: Class[_ <: Plane])

  case class GameCreated(gameContext: GameContext)

  case class StartGame(user: UserInfo)

  case object GameStarted

  case class Ask(userId: TeamMail)

}
package fr.xebia.xke.akka.game

import akka.actor._
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.game.GameStore._
import fr.xebia.xke.akka.infrastructure.SessionId
import fr.xebia.xke.akka.infrastructure.SessionInfo

class GameStore extends Actor with ActorLogging {

  var gameContexts: Map[SessionId, GameContext] = _
  var gameCounter: Int = _

  implicit val timeout = Timeout(1 second)


  override def preStart() {
    gameContexts = Map.empty
    gameCounter = 0
  }

  def receive: Receive = {

    case NewGame(userInfo, settings, planeType) =>
      newGame(userInfo, settings, planeType)

    case StartGame(userInfo, airTrafficControl, groundControl) =>
      startGame(userInfo, airTrafficControl, groundControl)

    case event@PlayerUp(sessionId, address) if gameContexts.contains(sessionId) =>
      gameContexts(sessionId).publish(event)

    case event@PlayerDown(sessionId, address) if gameContexts.contains(sessionId) =>
      gameContexts(sessionId).publish(event)

    case Ask(sessionId) =>
      sender ! gameContexts.get(sessionId)

    case default =>
      log.info(default.toString)
  }

  def newGame(userInfo: SessionInfo, settings: Settings, planeType: Class[_ <: Plane]) {
    for (gameContext <- gameContexts.get(userInfo.sessionId)) {

      gameContext.stop(context.system)

      gameContexts -= userInfo.sessionId
    }

    val sessionId = s"game-session-${userInfo.airportCode}-$gameCounter"

    val gameContext = GameContext.create(sessionId, settings, planeType, userInfo.airport)(context)
    gameCounter += 1

    gameContexts += (userInfo.sessionId -> gameContext)

    log.info(s"Create a new game for <${userInfo.sessionId}>")

    sender ! GameCreated(gameContext)
  }

  def startGame(userInfo: SessionInfo, airTrafficControl: ActorRef, groundControl: ActorRef) {
    log.info("startGame")
    for (gameContext <- gameContexts.get(userInfo.sessionId)) {
      log.info(s"Start the game for <${userInfo.sessionId}>, session = <${gameContext.game.path.name}>")

      gameContext.init(airTrafficControl, groundControl)

      sender ! GameStarted
    }
  }
}

object GameStore {

  def props(): Props = Props[GameStore]

  case class NewGame(userInfo: SessionInfo, settings: Settings, planeType: Class[_ <: Plane])

  case class GameCreated(gameContext: GameContext)

  case class StartGame(userInfo: SessionInfo, airTrafficControl: ActorRef, groundControl: ActorRef)

  case object GameStarted

  case class Ask(sessionId: SessionId)

}
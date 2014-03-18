package controllers

import akka.actor._
import fr.xebia.xke.akka.airport.plane.Plane
import fr.xebia.xke.akka.airport._
import controllers.GameStore._
import akka.event.EventStream
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.airport.InitGame
import fr.xebia.xke.akka.airport.PlayerUp
import controllers.GameStore.NewGame
import controllers.GameStore.StartGame

class GameStore extends Actor with ActorLogging {

  var contexts: Map[TeamMail, GameContext] = _
  var gameCounter: Int = _

  implicit val timeout = Timeout(1 second)


  override def preStart() {
    contexts = Map.empty
    gameCounter = 0
  }

  def receive: Receive = {

    case NewGame(userInfo, settings, planeType) =>
      newGame(userInfo, settings, planeType)

    case StartGame(userInfo) if contexts.contains(userInfo.userId) =>
      startGame(userInfo)

    case event@PlayerUp(userId, address) if contexts.contains(userId) =>
      log.info(event.toString)
      contexts(userId).eventBus.publish(event)

    case Ask(userId) =>
      sender ! contexts.get(userId)
  }

  def newGame(userInfo: UserInfo, settings: Settings, planeType: Class[_ <: Plane]) {
    for (gameContext <- contexts.get(userInfo.userId)) {
      context.stop(gameContext.game)
      context.stop(gameContext.listener)

      contexts -= userInfo.userId
    }

    val eventStream = new EventStream(false)

    val listener = context.actorOf(Props(classOf[EventListener], eventStream))

    eventStream.subscribe(listener, classOf[GameEvent])
    eventStream.subscribe(listener, classOf[PlaneStatus])

    val session = s"game-session-${userInfo.airportCode}-$gameCounter"
    val game = context.actorOf(Props(classOf[Game], settings, planeType, eventStream), session)
    log.info(s"Create a new game for <${userInfo.userId}>, session = <$session>")
    gameCounter += 1

    for (address <- userInfo.playerSystemAddress) {
      eventStream publish PlayerUp(userInfo.userId, address)
    }

    contexts += (userInfo.userId -> GameContext(listener, game, eventStream))

    sender ! GameCreated
  }

  def startGame(user: UserInfo) {
    for {
      address <- user.playerSystemAddress
      gameContext <- contexts.get(user.userId)
    } {
      log.info(s"Start the game for <${user.userId}>, session = <${gameContext.game.path.name}>")

      val airTrafficControl = context.actorSelection(
        ActorPath.fromString(address.toString) / "user" / "airTrafficControl")

      val groundControl = context.actorSelection(
        ActorPath.fromString(address.toString) / "user" / "groundControl")

      gameContext.game ! InitGame(airTrafficControl, groundControl)

      sender ! GameStarted
    }
  }
}

object GameStore {

  def props(): Props = Props[GameStore]

  case class NewGame(user: UserInfo, settings: Settings, planeType: Class[_ <: Plane])

  case object GameCreated

  case class StartGame(user: UserInfo)

  case object GameStarted

  case class Ask(userId: TeamMail)

}
package fr.xebia.xke.akka.game

import akka.actor._
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.infrastructure.SessionId
import fr.xebia.xke.akka.airport.{Airport, AirportCode}
import fr.xebia.xke.akka.game.GameStore._
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.game.GameStore.NewGame
import fr.xebia.xke.akka.game.GameStore.Ask
import fr.xebia.xke.akka.game.GameStore.GameCreated
import fr.xebia.xke.akka.game.GameStore.StartGame
import akka.event.EventStream
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator
import akka.pattern.ask

class GameStore(airportsClusterLocation: ActorRef, clusterEventStream: EventStream) extends Actor with ActorLogging {

  var gameCounter: Int = _

  var gameContexts: Map[GameStatus, GameContext] = _

  import context.dispatcher

  implicit val timeout: Timeout = Timeout(10 seconds)


  override def preStart() {
    gameContexts = Map.empty
    gameCounter = 0

    clusterEventStream.subscribe(self, classOf[AirportLocator.AirportConnected])
    clusterEventStream.subscribe(self, classOf[AirportLocator.AirportDisconnected])
  }

  def receive: Receive = {

    case NewGame(airport, settings, planeType) =>
      newGame(airport, settings, planeType)

    case StartGame(sessionId) =>
      startGame(sessionId)

    case Ask(sessionId) =>
      sender() ! gameContexts.find(_._1.sessionId == sessionId).map(_._2)

    case AirportLocator.AirportConnected(airportCode, address) =>
      airportConnected(airportCode, address)

    case AirportLocator.AirportDisconnected(airportCode, address) =>
      airportDisconnected(airportCode, address)

    case Terminated(game) =>
      val existingGame = gameContexts.find(_._2.game == game).map(_._1)
      existingGame.foreach(gameContexts -= _)
  }

  def airportDisconnected(airportCode: AirportCode, address: Address) {
    for ((status, gameContext) <- gameContexts.filter(_._1.airportCode == airportCode)) {
      gameContext.publish(PlayerDown(address))
    }
  }

  def airportConnected(airportCode: AirportCode, address: Address) {
    for ((status, gameContext) <- gameContexts.filter(_._1.airportCode == airportCode)) {
      gameContext.publish(PlayerUp(address))

      if (status.started) {
        log.info(s"Game <${gameContext.sessionId}> already started")

        airportsClusterLocation ! AirportLocator.UpdateClient(gameContext.airport.code, gameContext.sessionId, address)

      } else {
        log.info(s"Game <${gameContext.sessionId}> is ready to start")
      }
    }
  }

  def newGame(airport: Airport, settings: Settings, planeType: Class[_ <: Plane]) {
    val gameContext = GameContext.create(settings, planeType, airport, airportsClusterLocation)(context)

    context watch gameContext.game

    gameCounter += 1

    gameContexts += (GameStatus(airport.code, gameContext.sessionId) -> gameContext)

    log.info(s"Create a new game for <${airport.code}> with id <${gameContext.sessionId}>")

    val lastSender = sender()

    for (airportAddress <- ask(airportsClusterLocation, AirportLocator.AskAddress(airport.code)).mapTo[Option[Address]]) {

      airportAddress.foreach(address => {
        gameContext.publish(PlayerUp(address))
      })
    }

    lastSender ! GameCreated(gameContext)
  }

  def startGame(sessionId: SessionId) {
    for ((status, gameContext) <- gameContexts.find(_._1.sessionId == sessionId)) {
      log.info(s"Start the game <${gameContext.game.path.name}>")

      def newAirport = ask(airportsClusterLocation, AirportLocator.CreateClient(gameContext.airport.code, sessionId)).mapTo[ActorRef]

      val lastSender = sender()

      for (airTrafficControl <- newAirport) {
        gameContext.init(airTrafficControl)

        lastSender ! GameStarted

        status.started = true

      }
    }
  }
}

object GameStore {

  def props(airportsClusterLocation: ActorRef, clusterEventStream: EventStream): Props = Props(classOf[GameStore], airportsClusterLocation, clusterEventStream)

  case class NewGame(airport: Airport, settings: Settings, planeType: Class[_ <: Plane])

  case class GameCreated(gameContext: GameContext)

  case class StartGame(sessionId: SessionId)

  case object GameStarted

  case class Ask(sessionId: SessionId)

  private[GameStore] case class GameStatus(airportCode: AirportCode, sessionId: SessionId) {
    var started: Boolean = false
  }

}
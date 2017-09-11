package controllers

import javax.inject.Inject

import akka.actor.{Actor, ActorRef, Props}
import akka.event.EventStream
import akka.pattern.ask
import akka.stream.ActorMaterializer
import fr.xebia.xke.akka.airport.{Airport, AirportCode}
import fr.xebia.xke.akka.game.GameStore.{GameCreated, StartGame}
import fr.xebia.xke.akka.game._
import fr.xebia.xke.akka.infrastructure._
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator
import fr.xebia.xke.akka.plane._
import play.api.inject.ApplicationLifecycle
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class Application @Inject()(lifecycle: ApplicationLifecycle) extends InjectedController with PlayerSessionManagement {

  val clusterEventStream = new EventStream(airportActorSystem)

  val airportsClusterLocation: ActorRef = airportActorSystem.actorOf(AirportLocator.props(clusterEventStream), "airportsClusterLocation")

  val gameStore: ActorRef = airportActorSystem.actorOf(GameStore.props(airportsClusterLocation, clusterEventStream), "gameStore")

  lifecycle.addStopHook(() => {
    airportActorSystem.terminate()
  })

  def createLevel1(airportCode: AirportCode) = Action {
    val settings = Settings(
      nrOfRunways = 1,
      landingMaxDuration = 1500,
      planeGenerationInterval = 5000,
      nrOfTaxiways = 2,
      taxiingDuration = 500,
      unloadingPassengersMaxDuration = 500,
      objective = 20,
      ackMaxDuration = 1000)

    newSinglePlayerGame(airportCode, settings, classOf[JustParkingPlane]) {
      gameContext => routes.Application.level1(airportCode, gameContext.sessionId)
    }
  }

  def createLevel2(airportCode: AirportCode) = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      nrOfTaxiways = 2,
      taxiingDuration = 1000,
      taxiwayCapacity = 5,
      nrOfGates = 2,
      ackMaxDuration = 1000)

    newSinglePlayerGame(airportCode, settings, classOf[JustParkingPlane]) {
      gameContext => routes.Application.level2(airportCode, gameContext.sessionId)
    }
  }

  def createLevel3(airportCode: AirportCode) = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      nrOfTaxiways = 2,
      taxiingDuration = 1000,
      taxiwayCapacity = 10,
      nrOfGates = 2,
      unloadingPassengersMaxDuration = 5000,
      ackMaxDuration = 1000,
      radioReliability = 0.8)
    newSinglePlayerGame(airportCode, settings, classOf[JustParkingPlane]) {
      gameContext => routes.Application.level3(airportCode, gameContext.sessionId)
    }
  }

  def createLevel4(airportCode: AirportCode) = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 250,
      objective = 100,
      nrOfTaxiways = 3,
      taxiingDuration = 1000,
      taxiwayCapacity = 10,
      nrOfGates = 4,
      unloadingPassengersMaxDuration = 5000,
      ackMaxDuration = 1000,
      radioReliability = 0.8,
      chaosMonkey = true)
    newSinglePlayerGame(airportCode, settings, classOf[JustParkingPlane]) {
      gameContext => routes.Application.level4(airportCode, gameContext.sessionId)
    }
  }

  def level1(airportCode: AirportCode, sessionId: SessionId) = LoggedInAction(airportCode) {
    implicit request =>
      val gameContext = Await.result(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]], 10 seconds)

      gameContext match {
        case Some(context) => Ok(views.html.level_1(gameContext.get))
        case None => Redirect(routes.Application.registered(airportCode))
      }
  }

  def level2(airportCode: AirportCode, sessionId: SessionId) = LoggedInAction(airportCode) {
    implicit request =>
      val gameContext = Await.result(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]], 10 seconds)

      gameContext match {
        case Some(context) => Ok(views.html.level_2(gameContext.get))
        case None => Redirect(routes.Application.registered(airportCode))
      }
  }

  def level3(airportCode: AirportCode, sessionId: SessionId) = LoggedInAction(airportCode) {
    implicit request =>
      val gameContext = Await.result(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]], 10 seconds)

      gameContext match {
        case Some(context) => Ok(views.html.level_3(gameContext.get))
        case None => Redirect(routes.Application.registered(airportCode))
      }
  }

  def level4(airportCode: AirportCode, sessionId: SessionId) = LoggedInAction(airportCode) {
    implicit request =>
      val gameContext = Await.result(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]], 10 seconds)

      gameContext match {
        case Some(context) => Ok(views.html.level_4(gameContext.get))
        case None => Redirect(routes.Application.registered(airportCode))
      }
  }

  def events(airportCode: AirportCode, sessionId: SessionId) = WebSocket.accept[String, String] {
    _ =>
      implicit val system = airportActorSystem
      implicit val mat = ActorMaterializer.create(system)
      ActorFlow.actorRef({ out => Props(new WebSocketActor(out, gameStore, sessionId)) })
  }

  private def newSinglePlayerGame(airportCode: AirportCode, settings: Settings, planeType: Class[_ <: Plane])(template: (GameContext => play.api.mvc.Call)) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    def checkAirportRegistration = {
      ask(airportStore, AirportStore.IsRegistered(airportCode)).mapTo[Option[Airport]]
    }

    def askGameCreation(airport: Airport) = {
      ask(gameStore, GameStore.NewGame(airport, settings, planeType)).mapTo[GameCreated]
    }

    val gameCreation: Future[GameContext] = for {
      airportRegistered <- checkAirportRegistration if airportRegistered.isDefined
      gameCreated <- askGameCreation(airportRegistered.get)
    } yield {
      gameCreated.gameContext
    }

    val gameContext = Await.result(gameCreation, atMost = 10.seconds)

    Redirect(template(gameContext))
  }

}

case object DequeueEvents

class WebSocketActor(out: ActorRef, gameStore: ActorRef, sessionId: SessionId) extends Actor {
  var listener: ActorRef = _
  var started = false

  override def preStart(): Unit = {
    gameStore ! GameStore.Ask(sessionId)
  }

  def receive = {
    case Some(GameContext(_,_,_,l,_,_)) =>
      this.listener = l
      this.listener ! DequeueEvents

    case Some(msg) if msg.isInstanceOf[String] =>
      out ! msg
      this.listener ! DequeueEvents

    case msg: String =>
      if (!started) {
        started = true
        gameStore ! StartGame(sessionId)
      }
  }
}
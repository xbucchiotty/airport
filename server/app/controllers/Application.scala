package controllers

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._
import fr.xebia.xke.akka.infrastructure._
import fr.xebia.xke.akka.game._
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import akka.pattern.ask
import scala.concurrent.{Future, Await}
import akka.actor.ActorRef
import fr.xebia.xke.akka.plane._
import fr.xebia.xke.akka.game.GameStore.GameCreated
import fr.xebia.xke.akka.game.GameStore.StartGame
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator
import fr.xebia.xke.akka.airport.{Airport, AirportCode}
import akka.event.EventStream

object Application extends Controller with PlayerSessionManagement {

  val clusterEventStream = new EventStream()

  val airportsClusterLocation: ActorRef = airportActorSystem.actorOf(AirportLocator.props(clusterEventStream), "airportsClusterLocation")

  val gameStore: ActorRef = airportActorSystem.actorOf(GameStore.props(airportsClusterLocation, clusterEventStream), "gameStore")

  def createLevel0(airportCode: AirportCode) = Action {
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
      gameContext => routes.Application.level0(airportCode, gameContext.sessionId)
    }
  }

  def createLevel1(airportCode: AirportCode) = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      nrOfTaxiways = 1,
      taxiingDuration = 1000,
      taxiwayCapacity = 5,
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
      taxiwayCapacity = 10,
      nrOfGates = 2,
      unloadingPassengersMaxDuration = 5000,
      ackMaxDuration = 1000,
      radioReliability = 0.8)
    newSinglePlayerGame(airportCode, settings, classOf[JustParkingPlane]) {
      gameContext => routes.Application.level2(airportCode, gameContext.sessionId)
    }
  }

  def createLevel3(airportCode: AirportCode) = Action {
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
      gameContext => routes.Application.level3(airportCode, gameContext.sessionId)
    }
  }

  def level0(airportCode: AirportCode, sessionId: SessionId) = LoggedInAction(airportCode) {
    implicit request =>
      val gameContext = Await.result(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]], 10 seconds)

      gameContext match {
        case Some(context) => Ok(views.html.level_0(gameContext.get))
        case None => Redirect(routes.Application.registered(airportCode))
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

  def events(airportCode: AirportCode, sessionId: SessionId) = WebSocket.tryAccept[String] {
    _ =>

      import scala.concurrent.ExecutionContext.Implicits.global

      val contextReply = ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]]

      for {
        context <- contextReply
      }
      yield {
        var started = false

        val in = Iteratee.foreach[String] {
          case "start" if !started =>
            gameStore ! StartGame(sessionId)

            started = true
        }

        val out = Enumerator.fromCallback1 {
          _ => ask(context.get.listener, DequeueEvents)(Timeout(10 minutes)).mapTo[Option[String]]
        }

        Right(in, out)
      }
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
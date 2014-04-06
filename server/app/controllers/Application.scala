package controllers

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._
import play.api.templates.HtmlFormat
import fr.xebia.xke.akka.infrastructure._
import fr.xebia.xke.akka.game._
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import akka.pattern.ask
import scala.concurrent.{Future, Await}
import akka.actor.{ActorRef, Address}
import fr.xebia.xke.akka.plane._
import play.api.libs.json.Json
import fr.xebia.xke.akka.game.GameStore.Ask
import fr.xebia.xke.akka.game.GameStore.GameCreated
import fr.xebia.xke.akka.game.PlayerUp
import scala.Some
import fr.xebia.xke.akka.game.GameStore.StartGame
import fr.xebia.xke.akka.infrastructure.SessionInfo
import fr.xebia.xke.akka.infrastructure.cluster.{AirportProxy, AirportLocator}

object Application extends Controller with PlayerSessionManagement {

  val gameStore: ActorRef = airportActorSystem.actorOf(GameStore.props(), "gameStore")

  val airportsClusterLocation: ActorRef = airportActorSystem.actorOf(AirportLocator.props(sessionStore, gameStore), "airports")

  def level0(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 1,
          landingMaxDuration = 1500,
          planeGenerationInterval = 3000,
          objective = 100,
          ackMaxDuration = 1000)

        newSinglePlayerGame(sessionId)(settings, views.html.level_0(settings, userInfo), classOf[JustLandingPlane])
  }

  def level1(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>

        val settings = Settings(
          nrOfRunways = 2,
          landingMaxDuration = 1500,
          planeGenerationInterval = 1250,
          objective = 20,
          ackMaxDuration = 1000)

        newSinglePlayerGame(sessionId)(settings, views.html.level_1(settings, userInfo), classOf[JustLandingPlane])
  }

  def level2(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>

        val settings = Settings(
          nrOfRunways = 4,
          landingMaxDuration = 2500,
          planeGenerationInterval = 500,
          objective = 50,
          ackMaxDuration = 1000)

        newSinglePlayerGame(sessionId)(settings, views.html.level_2(settings, userInfo), classOf[JustLandingPlane])
  }

  def level3(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>

        val settings = Settings(
          nrOfRunways = 4,
          landingMaxDuration = 2500,
          planeGenerationInterval = 500,
          objective = 50,
          nrOfTaxiways = 1,
          taxiingDuration = 1000,
          taxiwayCapacity = 5,
          ackMaxDuration = 1000)

        newSinglePlayerGame(sessionId)(settings, views.html.level_3(settings, userInfo), classOf[JustTaxiingPlane])
  }

  def level4(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>

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
          ackMaxDuration = 1000)

        newSinglePlayerGame(sessionId)(settings, views.html.level_4(settings, userInfo), classOf[JustParkingPlane])
  }

  def level5(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>

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
        newSinglePlayerGame(sessionId)(settings, views.html.level_5(settings, userInfo), classOf[JustParkingPlane])
  }

  def level6(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      implicit request =>

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
          radioReliability = 0.8)
        newMultiplayerGame(sessionId)(settings, views.html.level_6(settings, userInfo), classOf[MultiAirportPlane])
  }

  def multiplayerWithoutUser = multiplayer(None)

  def multiplayerWithUser(sessionId: SessionId) = multiplayer(Some(sessionId))

  def multiplayer(sessionId: Option[SessionId]) = Action {
    val userInfo: Option[SessionInfo] = sessionId.flatMap(session => currentSessionInfo(session))
    Ok(views.html.multiplayer(userInfo.map(_.airport)))
  }

  def scores = Action {
    val airportScores = airports.take(10).zipWithIndex.map {
      case (airport, index) => AirportScore(
        airport.code,
        airport.latitude.toDouble,
        airport.longitude.toDouble,
        (index + 20) / 1000d)
    }.toSeq

    Ok(Json.toJson(AirportScores(airportScores)))
  }

  def events(sessionId: SessionId) = WebSocket.async[String] {
    _ =>

      val userInfo = currentSessionInfo(sessionId).get
      // Log events to the console

      import scala.concurrent.ExecutionContext.Implicits.global
      import akka.pattern.AskTimeoutException

      val contextReply = ask(gameStore, Ask(userInfo.sessionId)).mapTo[Option[GameContext]]

      for {
        context <- contextReply
      }
      yield {

        val in = Iteratee.foreach[String] {
          case "start" =>
            val proxyReply = ask(airportsClusterLocation, AirportLocator.AskAirportAddressLookup(userInfo.airportCode)).mapTo[Option[AirportProxy]]

            for (proxy <- proxyReply) {

              val info = currentSessionInfo(sessionId).get
              gameStore ! StartGame(info, proxy.get.airTrafficControl, proxy.get.groundControl)
            }
        }

        val out = Enumerator.fromCallback1 {
          _ => ask(context.get.listener, DequeueEvents)(Timeout(10 minutes)).mapTo[Option[String]]
        }

        (in, out)
      }
  }

  private def newSinglePlayerGame(sessionId: SessionId)(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane]) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    for (userInfo <- currentSessionInfo(sessionId)) {
      val gameCreation = ask(gameStore, GameStore.NewGame(userInfo, settings, planeType)).mapTo[GameCreated]

      gameCreation.onSuccess {
        case GameCreated(gameContext) =>
          val proxyLookup = ask(airportsClusterLocation, AirportLocator.AskAirportAddressLookup(userInfo.airportCode)).mapTo[Option[AirportProxy]]
          proxyLookup.onSuccess {
            case Some(proxy) =>
              gameContext.eventBus.publish(PlayerUp(userInfo.sessionId, proxy.address))
          }
      }

      Await.result(gameCreation, atMost = 10.seconds)


    }
    Ok(template)
  }

  private def newMultiplayerGame(sessionId: SessionId)(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane]) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    for (userInfo <- currentSessionInfo(sessionId)) {
      val gameCreation = ask(gameStore, GameStore.NewGame(userInfo, settings, planeType)).mapTo[GameCreated]

      gameCreation.onSuccess {
        case GameCreated(gameContext) =>
          val addressLookup = ask(airportsClusterLocation, AirportLocator.AskAirportAddressLookup(userInfo.airportCode)).mapTo[Option[Address]]
          addressLookup.onSuccess {
            case Some(address) =>
              gameContext.eventBus.publish(PlayerUp(userInfo.sessionId, address))
          }
      }

      Await.result(gameCreation, atMost = 10.seconds)


    }
    Ok(template)
  }

}

case object DequeueEvents
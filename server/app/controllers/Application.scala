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
import akka.actor.Address
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.game.GameStore.Ask
import fr.xebia.xke.akka.game.GameStore.GameCreated
import fr.xebia.xke.akka.game.PlayerUp
import scala.Some
import fr.xebia.xke.akka.plane.JustLandingPlane
import fr.xebia.xke.akka.game.GameStore.StartGame
import fr.xebia.xke.akka.plane.JustTaxiingPlane
import fr.xebia.xke.akka.plane.FullStepPlane
import play.api.libs.json.Json
import fr.xebia.xke.akka.airport.Airport

object Application extends Controller with PlayerSessionManagement {

  def index: Action[AnyContent] =
    LoggedInAction(_ => _ => Redirect(routes.Application.level0))

  def level0 = LoggedInAction {
    userInfo =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 1,
          landingMaxDuration = 1500,
          planeGenerationInterval = 3000,
          objective = 20,
          ackMaxDuration = 1000)

        newGame(settings, views.html.level_0(settings, userInfo.airport), classOf[JustLandingPlane])

  }

  def level1 = LoggedInAction {
    userInfo =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 2,
          landingMaxDuration = 1500,
          planeGenerationInterval = 1250,
          objective = 20,
          ackMaxDuration = 1000)

        newGame(settings, views.html.level_1(settings, userInfo.airport), classOf[JustLandingPlane])
  }

  def level2 = LoggedInAction {
    userInfo =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 4,
          landingMaxDuration = 2500,
          planeGenerationInterval = 500,
          objective = 50,
          ackMaxDuration = 1000,
          outOfKerozenTimeout = 30000)

        newGame(settings, views.html.level_2(settings, userInfo.airport), classOf[JustLandingPlane])
  }

  def level3 = LoggedInAction {
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
          ackMaxDuration = 1000,
          outOfKerozenTimeout = 30000)

        newGame(settings, views.html.level_3(settings, userInfo.airport), classOf[JustTaxiingPlane])
  }

  def level4 = LoggedInAction {
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
          outOfKerozenTimeout = 30000)

        newGame(settings, views.html.level_4(settings, userInfo.airport), classOf[FullStepPlane])
  }

  def level5 = LoggedInAction {
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
          radioReliability = 0.8,
          outOfKerozenTimeout = 30000)

        newGame(settings, views.html.level_5(settings, userInfo.airport), classOf[FullStepPlane])
  }

  def level6 = Action {
    implicit request =>
      val user: Option[UserInfo] = currentUser(session)
      Ok(views.html.level_6(user.map(_.airport)))
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

  def events = WebSocket.async[String] {
    implicit request =>

      val user = currentUser(session).get
      // Log events to the console

      import scala.concurrent.ExecutionContext.Implicits.global
      import akka.pattern.AskTimeoutException

      val contextReply = ask(gameStore, Ask(user.userId)).mapTo[Option[GameContext]]

      for (context <- contextReply)
      yield {

        val in = Iteratee.foreach[String] {
          case "start" =>
            //refresh user to get access to the player address system
            gameStore ! StartGame(currentUser(session).get)
        }
        def dequeue:Future[Option[String]]= ask(context.get.listener, DequeueEvents)(Timeout(500 millisecond)).mapTo[Option[String]]recoverWith{case t:AskTimeoutException =>  dequeue}
        val out: Enumerator[String]=Enumerator.generateM[String]({
         dequeue
        })
        (in, out)
      }

  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane])(implicit request: play.api.mvc.Request[_]) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    for (user <- currentUser(session)) {
      val gameCreation = ask(gameStore, GameStore.NewGame(user, settings, planeType)).mapTo[GameCreated]

      gameCreation.onSuccess {
        case GameCreated(gameContext) =>
          val addressLookup = ask(airportsClusterLocation, AirportLocator.AirportAddressLookup(user.airportCode)).mapTo[Option[Address]]
          addressLookup.onSuccess {
            case Some(address) =>
              gameContext.eventBus.publish(PlayerUp(user.userId, address))
          }
      }

      Await.result(gameCreation, atMost = 10.seconds)


    }
    Ok(template)
  }

}

case object DequeueEvents
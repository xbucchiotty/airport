package controllers

import fr.xebia.xke.akka.airport._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._
import play.api.templates.HtmlFormat
import fr.xebia.xke.akka.airport.plane.{Plane, JustTaxiingPlane, JustLandingPlane, FullStepPlane}
import controllers.GameStore.{Ask, StartGame}
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Await

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

  def events = WebSocket.async[String] {
    implicit request =>

      val user = currentUser(session).get
      // Log events to the console

      import scala.concurrent.ExecutionContext.Implicits.global
      val contextReply = ask(gameStore, Ask(user.userId)).mapTo[Option[GameContext]]

      for (context <- contextReply)
      yield {

        val in = Iteratee.foreach[String] {
          case "start" => {
            gameStore ! StartGame(user)
          }
        }

        val out: Enumerator[String] = Enumerator2.infiniteUnfold(context.get.listener) {
          listener => {
            ask(context.get.listener, DequeueEvents)(Timeout(1 second))
              .mapTo[Option[String]]
              .map(replyOption => replyOption.map(reply => (listener, reply))
            )
          }
        }

        (in, out)
      }

  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane])(implicit request: play.api.mvc.Request[_]) = {
    for (user <- currentUser(session)) {
      Await.result(ask(gameStore, GameStore.NewGame(user, settings, planeType)).mapTo[GameStore.GameCreated.type], atMost = 1.second)
    }
    Ok(template)
  }

}

case object DequeueEvents
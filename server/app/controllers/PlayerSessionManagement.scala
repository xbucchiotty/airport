package controllers

import play.api.mvc.{AnyContent, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import language.postfixOps
import akka.util.Timeout
import fr.xebia.xke.akka.infrastructure._
import fr.xebia.xke.akka.infrastructure.SessionStore.Ask
import fr.xebia.xke.akka.infrastructure.SessionStore.Register
import fr.xebia.xke.akka.infrastructure.SessionStore.Registered
import scala.Some
import fr.xebia.xke.akka.infrastructure.UserInfo
import fr.xebia.xke.akka.game.GameStore
import fr.xebia.xke.akka.airport.Airport

trait PlayerSessionManagement {

  this: Controller =>

  implicit val timeout = Timeout(10 second)

  import ExecutionContext.Implicits.global

  val airports = Airport.top100

  val airportActorSystem: ActorSystem = ActorSystem.create("airportSystem")

  val sessionStore: ActorRef = airportActorSystem.actorOf(SessionStore.props(airports), "sessionStore")

  val gameStore: ActorRef = airportActorSystem.actorOf(GameStore.props(), "gameStore")

  val airportsClusterLocation: ActorRef = airportActorSystem.actorOf(AirportLocator.props(sessionStore, gameStore), "airports")

  def currentUserInfo(sessionId: SessionId): Option[UserInfo] =
    Await.result(
      ask(sessionStore, Ask(sessionId)).mapTo[Option[UserInfo]], atMost = 10.seconds)

  def LoggedInAction(sessionId: SessionId)(securedAction: (UserInfo => play.api.mvc.Request[_] => play.api.mvc.SimpleResult)): play.api.mvc.Action[play.api.mvc.AnyContent] = Action {
    implicit request =>
      currentUserInfo(sessionId) match {

        case Some(userInfo) =>
          securedAction(userInfo)(request)

        case None =>
          Ok(views.html.register(None))
      }
  }

  def displayRegisterPage = Action {
    Ok(views.html.register(None))
  }

  def register = Action {
    val sessionId = SessionId()

    val registration = ask(sessionStore, Register(sessionId)).mapTo[Registered]

    Await.result(registration.map {
      case _ => Redirect(routes.Application.registered(sessionId))
    }, atMost = 10.seconds)

  }

  def registered(sessionId: SessionId) = LoggedInAction(sessionId) {
    userInfo =>
      _ =>
        Ok(views.html.registered(userInfo))
  }

  def index = Action {
    Redirect(routes.Application.displayRegisterPage)
  }
}

package controllers

import play.api.mvc.{AnyContent, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import fr.xebia.xke.akka.airport.game._
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import language.postfixOps
import akka.util.Timeout
import scala.Some
import fr.xebia.xke.akka.airport.game.UserInfo
import fr.xebia.xke.akka.airport.game.UserStore.{Registered, Register, Ask}

trait PlayerSessionManagement {

  this: Controller =>

  implicit val timeout = Timeout(1 second)

  import ExecutionContext.Implicits.global

  val airportActorSystem: ActorSystem = ActorSystem.create("airportSystem")

  val userStore: ActorRef = airportActorSystem.actorOf(UserStore.props(), "userStore")

  val gameStore: ActorRef = airportActorSystem.actorOf(GameStore.props(), "gameStore")

  val airports: ActorRef = airportActorSystem.actorOf(AirportLocator.props(userStore, gameStore), "airports")

  def currentUser(session: play.api.mvc.Session): Option[UserInfo] =
    Await.result(session.get("email").map(userId => {
      ask(userStore, Ask(TeamMail(userId))).mapTo[Option[UserInfo]]
    }).getOrElse(Future.successful(None)), atMost = 1.second)

  def LoggedInAction(securedAction: (UserInfo => play.api.mvc.Request[_] => play.api.mvc.SimpleResult)): play.api.mvc.Action[play.api.mvc.AnyContent] = Action {
    implicit request =>
      currentUser(session) match {

        case Some(user) =>
          securedAction(user)(request)

        case None =>
          Ok(views.html.register(HostName.from(request))(None))
      }
  }

  def register = Action {
    implicit request => {

      currentUser(session) match {

        case Some(_) =>

          Redirect(routes.Application.index())

        case None => {

          val form: Form[String] = Form(single("email" -> email))
          val userId = TeamMail(form.bindFromRequest().get)

          val registration = ask(userStore, Register(userId)).mapTo[Registered]

          Await.result(registration.map {
            case _ => Redirect(routes.Application.registered).withSession("email" -> userId.value)
          }, atMost = 10.seconds)
        }
      }
    }
  }

  def registered = LoggedInAction {
    userInfo => _ => {

      Ok(views.html.registered(userInfo))
    }
  }


  def index: Action[AnyContent]
}

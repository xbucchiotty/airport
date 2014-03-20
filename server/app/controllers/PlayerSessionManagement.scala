package controllers

import play.api.mvc.{AnyContent, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.Cluster
import fr.xebia.xke.akka.airport.Airport
import akka.pattern.ask
import fr.xebia.xke.akka.airport.game._
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import language.postfixOps
import akka.util.Timeout
import fr.xebia.xke.akka.airport.game.PlayerStore.Ask
import fr.xebia.xke.akka.airport.game.PlayerStore.Register
import scala.Some
import fr.xebia.xke.akka.airport.game.PlayerStore.Registered
import fr.xebia.xke.akka.airport.game.UserInfo

trait PlayerSessionManagement {

  this: Controller =>

  implicit val timeout = Timeout(1 second)

  import ExecutionContext.Implicits.global

  val airportActorSystem: ActorSystem = ActorSystem.create("airportSystem")

  val airports: ActorRef = airportActorSystem.actorOf(AirportLocator.props(), "airports")

  val gameStore: ActorRef = airportActorSystem.actorOf(GameStore.props(airports), "gameStore")

  val playerStore: ActorRef = airportActorSystem.actorOf(PlayerStore.props(gameStore, airports), "playerStore")

  val playerWatcher: ActorRef = {
    val playerWatcher = airportActorSystem.actorOf(PlayerWatcher.props(playerStore), "playerWatcher")
    Cluster(airportActorSystem).subscribe(playerWatcher, classOf[ClusterDomainEvent])
    playerWatcher
  }

  val allAirports: Map[String, Airport] = Airport.top100.groupBy(_.code).mapValues(_.head)

  def currentUser(session: play.api.mvc.Session): Option[UserInfo] =
    Await.result(session.get("email").map(userId => {
      ask(playerStore, Ask(TeamMail(userId))).mapTo[Option[UserInfo]]
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

          val registration = ask(playerStore, Register(userId)).mapTo[Registered]

          Await.result(registration.map {
            case _ => Redirect(routes.Application.registered).withSession("email" -> userId.value)
          }, atMost = 10.seconds)
        }
      }
    }
  }

  def registered = LoggedInAction {
    userInfo => _ => {

      val airport = allAirports(userInfo.airportCode)
      Ok(views.html.registered(userInfo, airport))
    }
  }


  def index: Action[AnyContent]
}

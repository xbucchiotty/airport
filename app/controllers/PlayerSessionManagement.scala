package controllers

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import fr.xebia.xke.akka.airport.{Airport, AirportCode}
import fr.xebia.xke.akka.infrastructure.AirportStore.{Register, Registered}
import fr.xebia.xke.akka.infrastructure._
import play.api.mvc.InjectedController

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

trait PlayerSessionManagement {

  this: InjectedController =>

  implicit val timeout = Timeout(10 second)

  import ExecutionContext.Implicits.global

  val airports = Airport.top100

  val airportActorSystem: ActorSystem = ActorSystem.create("airportSystem", ConfigFactory.load().getConfig("airportSystem"))

  val airportStore: ActorRef = airportActorSystem.actorOf(AirportStore.props(airports), "airportStore")

  def checkAirport(airportCode: AirportCode): Option[Airport] =
    Await.result(
      ask(airportStore, AirportStore.IsRegistered(airportCode)).mapTo[Option[Airport]], atMost = 10.seconds)


  def LoggedInAction(airportCode: AirportCode)(securedAction: (play.api.mvc.Request[_] => play.api.mvc.Result)): play.api.mvc.Action[play.api.mvc.AnyContent] = Action {
    implicit request: play.api.mvc.Request[_] =>
      checkAirport(airportCode) match {
        case Some(airport) =>
          securedAction(request)
        case None =>
          Redirect(routes.Application.displayRegisterPage)
      }
  }

  def register = Action {
    val registration = ask(airportStore, Register).mapTo[Registered]

    Await.result(registration.map {
      case Registered(airport) => Redirect(routes.Application.registered(airport.code))
    }, atMost = 10.seconds)

  }

  def registered(airportCode: AirportCode) = LoggedInAction(airportCode) {
    _ =>
      Ok(views.html.registered(airports.find(_.code == airportCode).get))
  }

  def displayRegisterPage = Action {
    Ok(views.html.register(None))
  }
}

package controllers

import play.api.mvc.{Action, Controller}
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration._
import language.postfixOps
import akka.util.Timeout
import fr.xebia.xke.akka.infrastructure._
import fr.xebia.xke.akka.infrastructure.AirportStore.Register
import fr.xebia.xke.akka.infrastructure.AirportStore.Registered
import fr.xebia.xke.akka.airport.{AirportCode, Airport}

trait PlayerSessionManagement {

  this: Controller =>

  implicit val timeout = Timeout(10 second)

  import ExecutionContext.Implicits.global

  val airports = Airport.top100

  val airportActorSystem: ActorSystem = ActorSystem.create("airportSystem")

  val airportStore: ActorRef = airportActorSystem.actorOf(AirportStore.props(airports), "airportStore")

  def checkAirport(airportCode: AirportCode): Option[Airport] =
    Await.result(
      ask(airportStore, AirportStore.IsRegistered(airportCode)).mapTo[Option[Airport]], atMost = 10.seconds)


  def LoggedInAction(airportCode: AirportCode)(securedAction: (play.api.mvc.Request[_] => play.api.mvc.Result)): play.api.mvc.Action[play.api.mvc.AnyContent] = Action {
    implicit request =>
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

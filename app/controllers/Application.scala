package controllers

import akka.actor.{Inbox, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport.{GameStart, Plane, JustLandingPlane, GameEvent, Settings, Game}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Iteratee
import play.api.mvc._
import play.api.templates.HtmlFormat

object Application extends Controller {

  def level0 = Action {
    val settings = Settings(
      nrOfRunways = 1,
      landingMaxDuration = 1500,
      planeGenerationInterval = 3000,
      objective = 20,
      ackMaxDuration = 500)

    newGame(settings, views.html.level_0(settings), classOf[JustLandingPlane])
  }

  def level1 = Action {
    val settings = Settings(
      nrOfRunways = 2,
      landingMaxDuration = 1500,
      planeGenerationInterval = 1250,
      objective = 20,
      ackMaxDuration = 500)

    newGame(settings, views.html.level_1(settings), classOf[JustLandingPlane])
  }

  def level2 = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      ackMaxDuration = 100)

    newGame(settings, views.html.level_2(settings), classOf[JustLandingPlane])
  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane]) = {

    if (game != null) {
      system.stop(game)
      game = null
    }

    game = system.actorOf(Props(classOf[Game], settings, planeType))

    if (listener != null) {
      system.eventStream.unsubscribe(listener)
      system.stop(listener)
    }

    listener = system.actorOf(Props[EventListener])

    system.eventStream.subscribe(listener, classOf[PlaneStatus])
    system.eventStream.subscribe(listener, classOf[GameEvent])

    Ok(template)
  }

  def index = Action {
    Redirect(routes.Application.level0)
  }

  def events = WebSocket.using[String] {
    request =>

    // Log events to the console
      import scala.concurrent.ExecutionContext.Implicits.global
      val in = Iteratee.foreach[String] {
        case "start" =>
          game.tell(GameStart, Inbox.create(system).getRef())
      }

      val out = Enumerator2.infiniteUnfold(listener) {
        listener =>
          ask(listener, DequeueEvents)(Timeout(1 second))
            .mapTo[Option[String]]
            .map(replyOption => replyOption
            .map(reply => (listener, reply))
          )
      }

      (in, out)
  }

  import play.api.Play.current

  val system = Akka.system

  var listener: ActorRef = null
  var game: ActorRef = null
}

case object DequeueEvents
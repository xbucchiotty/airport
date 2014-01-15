package controllers

import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport.{Plane, JustLandingPlane, GameEvent, Settings, Game}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Iteratee
import play.api.mvc._
import scala.Some

object Application extends Controller {

  private val steps: Seq[GameStrategy] = Seq(

    GameStrategy(Settings(
      nrOfRunways = 1,
      landingMaxDuration = 1500,
      planeGenerationInterval = 3000,
      objective = 20,
      ackMaxDuration = 500),
      Seq("Runway"), classOf[JustLandingPlane]),

    GameStrategy(Settings(
      nrOfRunways = 2,
      landingMaxDuration = 1500,
      planeGenerationInterval = 1250,
      objective = 20,
      ackMaxDuration = 500),
      Seq("Runway"), classOf[JustLandingPlane]),

    GameStrategy(Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      ackMaxDuration = 100),
      Seq("Runway"), classOf[JustLandingPlane])
  )

  def newGame(level: Int) = Action {

    if (game != null) {
      system.stop(game)
      game = null
    }

    val strategy = steps(level)

    game = system.actorOf(Props(classOf[Game], strategy.settings, strategy.planeType))

    if (listener != null) {
      system.eventStream.unsubscribe(listener)
      system.stop(listener)
    }

    listener = system.actorOf(Props[EventListener])

    system.eventStream.subscribe(listener, classOf[PlaneStatus])
    system.eventStream.subscribe(listener, classOf[GameEvent])

    Ok(views.html.index(strategy)(level, if (level < steps.length - 1) Some(level + 1) else None))
  }

  def index = Action {
    Redirect(routes.Application.newGame(0))
  }

  def events = WebSocket.using[String] {
    request =>

    // Log events to the console
      import scala.concurrent.ExecutionContext.Implicits.global
      val in = Iteratee.foreach[String](println).map {
        _ =>
          println("Disconnected")
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

case class GameStrategy(settings: Settings, activeSteps: Seq[String], planeType: Class[_ <: Plane])
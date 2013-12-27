package controllers

import akka.actor.{Inbox, ActorRef, Props, Actor}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport.Game.NewPlane
import fr.xebia.xke.akka.airport.{FlightEvent, UIEvent, GameConfiguration, Game}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.Enumerator.TreatCont1
import play.api.libs.iteratee.{Input, Enumerator, Iteratee}
import play.api.mvc._
import scala.Some
import scala.concurrent.{ExecutionContext, Future}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  import play.api.Play.current

  val system = Akka.system
  val listener = system.actorOf(Props[Listener])
  var game: ActorRef = null
  system.eventStream.subscribe(listener, classOf[UIEvent])

  def events = WebSocket.using[String] {
    request =>

    // Log events to the console
      import scala.concurrent.ExecutionContext.Implicits.global
      val in = Iteratee.foreach[String](println).map {
        _ =>
          println("Disconnected")
      }

      val out = Enumerator2.infineUnfold(listener) {
        listener =>
          ask(listener, Events)(Timeout(1 second))
            .mapTo[Option[String]]
            .map(replyOption => replyOption
            .map(reply => (listener, reply))
          )
      }

      (in, out)
  }

  def reset = Action {
    if (game != null) {
      system.stop(game)
      game = null
    }

    Redirect(routes.Application.start())
  }

  def start = Action {
    if (game == null) {
      game = system.actorOf(Props(classOf[Game], GameConfiguration()), name = "game")
    }
    Ok
  }

  def newPlane = Action {
    if (game != null) {
      val inbox = Inbox.create(system)
      inbox.send(game, NewPlane)
    }
    Ok
  }
}

class Listener extends Actor {

  private var buffer = List.empty[String]

  def receive = {
    case FlightEvent(evt, name) =>
      buffer = s"$evt:$name" :: buffer

    case Events =>
      sender ! buffer.headOption

      if (buffer.nonEmpty) {
        buffer = buffer.tail
      }
  }
}

case object Events

case class GameEvent(message: String)

object Enumerator2 {
  /**
   * Like [[play.api.libs.iteratee.Enumerator.unfold]], but allows the unfolding to be done asynchronously.
   *
   * @param s The value to unfold
   * @param f The unfolding function. This will take the value, and return a future for some tuple of the next value
   *          to unfold and the next input, or none if the value is completely unfolded.
   *          $paramEcSingle
   */
  def infineUnfold[S, E](s: S)(f: S => Future[Option[(S, E)]])(implicit ec: ExecutionContext): Enumerator[E] = Enumerator.checkContinue1(s)(new TreatCont1[E, S] {
    val pec = ec.prepare()

    def apply[A](loop: (Iteratee[E, A], S) => Future[Iteratee[E, A]], s: S, k: Input[E] => Iteratee[E, A]): Future[Iteratee[E, A]] = {
      f(s).flatMap {
        case Some((newS, e)) => loop(k(Input.El(e)), newS)
        case None => Thread.sleep(100); loop(k(Input.Empty), s)
      }(ExecutionContext.global)
    }
  })

}
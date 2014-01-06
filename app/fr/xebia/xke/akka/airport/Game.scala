package fr.xebia.xke.akka.airport

import akka.actor.{Cancellable, Terminated, ActorLogging, Props, Actor}
import controllers.PlaneStatus
import fr.xebia.xke.akka.airport.Game.NewPlane
import languageFeature.postfixOps
import scala.util.Random
import concurrent.duration._

class Game(settings: Settings) extends Actor with ActorLogging {

  val runway = context.actorOf(Props[Runway], "runway-1")
  val taxiway = context.actorOf(Props(classOf[Taxiway], settings), "taxiway-Z")
  val gate = context.actorOf(Props[Gate], "gate-1")

  val groundControl = context.actorOf(Props(classOf[GroundControl], taxiway, gate), "groundControl")
  val airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl, runway), "airTrafficControl")

  var planeGeneration: Cancellable = null

  var score = 0

  override def preStart() {
    context watch runway
    context watch taxiway
    context watch gate

    import context.dispatcher
    planeGeneration = context.system.scheduler.schedule(1 second, settings.planeGenerationInterval milliseconds, self, NewPlane)

    context.system.eventStream.subscribe(self, classOf[PlaneStatus])

    publishScore()
  }


  def receive: Receive = {
    case PlaneStatus("done", _, _, _) =>
      gain()

    case PlaneStatus(_, _, _, error) if error.nonEmpty =>
      loose()

    case Terminated(_) =>
      context.system.eventStream.publish(GameOver)
      context stop self

    case NewPlane =>
      context.actorOf(Props(classOf[Plane], airTrafficControl, self, settings), s"AF-${ Random.nextLong() % 100000 }")
  }

  private def publishScore() {
    context.system.eventStream.publish(Score(score, settings.objective))
  }

  private def gain() {
    score = Math.min(score + 2, settings.objective)
    publishScore()

    if (score == settings.objective) {
      context.system.eventStream.publish(GameEnd)
      planeGeneration.cancel()

      context stop self
    }
  }

  private def loose() {
    score = Math.max(score - 3, 0)
    publishScore()
  }

}

object Game {

  case object NewPlane

  case class ErrorInGame(cause: String) extends Exception(cause)

}
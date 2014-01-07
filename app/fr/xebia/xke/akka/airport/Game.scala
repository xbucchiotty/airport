package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Cancellable, Terminated, ActorLogging, Props, Actor}
import concurrent.duration._
import controllers.PlaneStatus
import fr.xebia.xke.akka.airport.Game.NewPlane
import languageFeature.postfixOps
import scala.util.Random

class Game(settings: Settings) extends Actor with ActorLogging {

  import settings._

  val runways: Seq[ActorRef] =
    for (i <- 0 to nrOfRunways) yield context.actorOf(Props[Runway], s"runway-$i")

  val taxiways: Seq[ActorRef] =
    for (i <- 0 to nrOfTaxiways) yield context.actorOf(Props(classOf[Taxiway], settings), s"taxiway-$i")

  val gates: Seq[ActorRef] =
    for (i <- 0 to nrOfRunways) yield context.actorOf(Props[Gate], s"gate-$i")


  val groundControl = context.actorOf(Props(classOf[GroundControl], taxiways, gates), "groundControl")
  val airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl, runways), "airTrafficControl")

  var planeGeneration: Cancellable = null

  var score = 0

  override def preStart() {
    runways.foreach(context.watch)
    taxiways.foreach(context.watch)
    gates.foreach(context.watch)

    import context.dispatcher
    planeGeneration = context.system.scheduler.schedule(1 second, planeGenerationInterval milliseconds, self, NewPlane)

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
    context.system.eventStream.publish(Score(score, objective))
  }

  private def gain() {
    score = Math.min(score + 2, objective)
    publishScore()

    if (score == objective) {
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
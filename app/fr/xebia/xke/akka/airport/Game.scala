package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Props, ActorRef, Actor}
import controllers.PlaneStatus
import fr.xebia.xke.akka.airport.Game.NewPlane
import languageFeature.postfixOps
import scala.util.Random

class Game(settings: Settings) extends Actor with ActorLogging {

  val runway = context.actorOf(Props[Runway], "runway-1")
  val taxiway = context.actorOf(Props(classOf[Taxiway], settings), "taxiway-Z")
  val gate = context.actorOf(Props[Gate], "gate-1")

  val groundControl = context.actorOf(Props(classOf[GroundControl], taxiway, gate), "groundControl")
  val airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl, runway), "airTrafficControl")

  var planes = Vector.empty[ActorRef]

  var score = 0

  override def preStart() {
    /*import context.dispatcher
    context.system.scheduler.schedule(1 second, 5 seconds, self, NewPlane)*/

    context.system.eventStream.subscribe(self, classOf[PlaneStatus])

    publishScore()
  }


  def receive: Receive = {
    case PlaneStatus("done", _, _, _) =>
      score += 5
      publishScore()

    case PlaneStatus(_, _, _, error) if error.nonEmpty =>
      score -= 2
      publishScore()

    case NewPlane =>
      context.actorOf(Props(classOf[Plane], airTrafficControl, self, settings), s"AF-${ Random.nextLong() % 10000 }")
  }

  private def publishScore() {
    context.system.eventStream.publish(Score(score, 50))
  }

}

object Game {

  case object NewPlane

  case class ErrorInGame(cause: String) extends Exception(cause)

}
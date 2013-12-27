package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Terminated, Props, ActorRef, Actor}
import languageFeature.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.airport.Game.{ErrorInGame, NewPlane}
import fr.xebia.xke.akka.airport.Event.Score

class Game(config: GameConfiguration = GameConfiguration()) extends Actor with ActorLogging {

  val runway = context.actorOf(Props[Runway], "runway-1")
  val taxiway = context.actorOf(Props(classOf[Taxiway], config.taxiwayCapacity), "taxiway-Z")
  val gate = context.actorOf(Props[Gate], "gate-1")

  val groundControl = context.actorOf(Props(classOf[GroundControl], taxiway, gate), "groundControl")
  val airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl, runway), "airTrafficControl")

  var planes = Set.empty[ActorRef]

  var score = 0

  override def preStart() {
    context watch runway
    context watch gate
    context watch taxiway

    import context.dispatcher
    context.system.scheduler.schedule(1 second, 5 seconds, self, NewPlane)
  }

  def receive: Receive = {
    case Terminated(ref) =>
      throw ErrorInGame(ref.path.name)

    case NewPlane =>
      val newPlane = context.actorOf(Props(classOf[Plane], airTrafficControl, self), s"AF-${ planes.size }")
      context watch newPlane
      planes += newPlane

    case Score(points) =>
      log.info(s"unwatch $sender")
      context unwatch sender
      score += points
  }

}

case class GameConfiguration(nrOfRunways: Int = 1, taxiwayCapacity: Int = 10, nrOfGates: Int = 1)

object Game {

  case object NewPlane

  case class ErrorInGame(cause: String) extends Exception(cause)

}
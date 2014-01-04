package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Terminated, Props, ActorRef, Actor}
import fr.xebia.xke.akka.airport.Game.NewPlane
import languageFeature.postfixOps

class Game(settings: Settings) extends Actor with ActorLogging {

  val runway = context.actorOf(Props[Runway], "runway-1")
  val taxiway = context.actorOf(Props(classOf[Taxiway], settings), "taxiway-Z")
  val gate = context.actorOf(Props[Gate], "gate-1")

  val groundControl = context.actorOf(Props(classOf[GroundControl], taxiway, gate), "groundControl")
  val airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl, runway), "airTrafficControl")

  var planes = Vector.empty[ActorRef]

  var score = 0

  override def preStart() {
    context watch runway
    context watch gate
    context watch taxiway

    /*import context.dispatcher
    context.system.scheduler.schedule(1 second, 5 seconds, self, NewPlane)*/
  }

  def receive: Receive = {
    case Terminated(_) =>
      //context stop self

    case NewPlane =>
      val newPlane = context.actorOf(Props(classOf[Plane], airTrafficControl, self, settings), s"AF-${ planes.size }")
      context watch newPlane
      planes = planes :+ newPlane

  }

}

object Game {

  case object NewPlane

  case class ErrorInGame(cause: String) extends Exception(cause)

}
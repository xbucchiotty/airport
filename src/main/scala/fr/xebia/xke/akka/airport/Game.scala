package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Terminated, Props, ActorRef, Actor}

class Game(config: GameConfiguration) extends Actor with ActorLogging {

  val runway = context.actorOf(Props[Runway], "runway-1")
  val taxiway = context.actorOf(Props(classOf[Taxiway], config.taxiwayCapacity), "taxiway-Z")
  val gate = context.actorOf(Props[Gate], "gate-1")

  val groundControl = context.actorOf(Props(classOf[GroundControl], taxiway, gate), "groundControl")
  val airTrafficControl = context.actorOf(Props(classOf[AirTrafficControl], groundControl, runway), "airTrafficControl")

  var planes = List.empty[ActorRef]

  override def preStart() {
    context watch groundControl
    context watch airTrafficControl
    context watch runway
    context watch gate
    context watch taxiway
  }

  def receive: Receive = {
    case Terminated(ref) =>
      log.info("Game terminates because of {}", ref.path.name)
      context stop self

  }

}

case class GameConfiguration(nrOfRunways: Int = 1, taxiwayCapacity: Int = 10, nrOfGates: Int = 1)

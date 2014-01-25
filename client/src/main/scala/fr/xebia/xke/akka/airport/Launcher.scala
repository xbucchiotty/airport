import akka.actor.{Props, ActorSystem}

import fr.xebia.xke.akka.airport.{AirTrafficControl, GroundControl}

object Launcher {

  def main(args: Array[String]) {
    val system = ActorSystem.create("airportSystem")

    val groundControl = system.actorOf(Props[GroundControl], "groundControl")

    system.actorOf(Props(classOf[AirTrafficControl], groundControl), "airTrafficControl")
  }


}

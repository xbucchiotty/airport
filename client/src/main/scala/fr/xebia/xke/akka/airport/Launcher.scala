import akka.actor.{Props, ActorSystem}

import fr.xebia.xke.akka.airport.{Airport, AirTrafficControl, GroundControl}

object Launcher {

  def main(args: Array[String]) {
    val system = ActorSystem.create("airportSystem")

    system.actorOf(Props[Airport], "airport")
  }


}

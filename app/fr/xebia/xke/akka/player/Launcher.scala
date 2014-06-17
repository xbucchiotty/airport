package fr.xebia.xke.akka.player

import akka.actor.ActorSystem

import com.typesafe.config.ConfigFactory
import collection.JavaConversions._

object Launcher {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load().getConfig("player")

    val airportCode = conf.getStringList("akka.cluster.roles").headOption.getOrElse("")
    if (airportCode.isEmpty) {
      println()
      println()
      println(s"You must provide an airport code in build.sbt before you can start the airport")
      println()
      println()
    } else {
      val system = ActorSystem.create("airportSystem", conf)
      system.actorOf(AirportManager.props, airportCode)

      println()
      println()
      println(s"Airport $airportCode started")
      println()
      println()

    }
  }

}

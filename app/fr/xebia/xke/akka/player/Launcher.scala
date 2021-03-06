package fr.xebia.xke.akka.player

import akka.actor.ActorSystem

import com.typesafe.config.ConfigFactory
import collection.JavaConversions._

object Launcher {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load().getConfig("player")

      val system = ActorSystem.create("airportSystem", conf)
      system.actorOf(AirportManager.props, "LHR")

      println()
      println()
      println("Airport started")
      println()
      println()

  }

}

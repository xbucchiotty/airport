package fr.xebia.xke.akka.airport

import akka.actor.{Props, Actor}
import language.postfixOps

class AirportManager extends Actor {

  def receive: Receive = {
    case NewGameInstance(sessionId) =>

      val newAirTrafficControl = context.actorOf(Props[AirTrafficControl], s"airTrafficControl-$sessionId")

      sender ! GameInstance(newAirTrafficControl)

    case ChaosMonkey =>
      println()
      println()
      println(s"Killed by a chaos monkey")
      println()
      println()

      sys.exit()
  }
}

object AirportManager {

  def props = Props[AirportManager]
}

package fr.xebia.xke.akka.player

import akka.actor._
import language.postfixOps
import fr.xebia.xke.akka.airport.message.{ChaosMonkey, NewGameInstance, GameInstance}

class AirportManager extends Actor with ActorLogging {

  def receive: Receive = {
    case NewGameInstance(sessionId) =>

      val newAirTrafficControl = context.actorOf(Props[AirTrafficControl], s"airTrafficControl-$sessionId")

      log.info(s"ATC-$sessionId created")


      sender ! GameInstance(newAirTrafficControl)

  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(loggingEnabled = true) {
    case _: ChaosMonkeyException =>
      SupervisorStrategy.Restart
  }
}

object AirportManager {

  def props = Props[AirportManager]
}

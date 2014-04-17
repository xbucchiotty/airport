package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class NewGameInstance(sessionId: String)

case class GameInstance(airTrafficControlRef: ActorRef)

case object ChaosMonkey

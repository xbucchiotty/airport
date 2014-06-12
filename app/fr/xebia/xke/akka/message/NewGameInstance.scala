package fr.xebia.xke.akka.airport.message

import akka.actor.ActorRef

case class NewGameInstance(sessionId: String)

case class GameInstance(airTrafficControlRef: ActorRef)
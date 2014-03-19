package fr.xebia.xke.akka.airport.game

import akka.actor.ActorRef
import akka.event.EventStream

case class GameContext(listener: ActorRef, game: ActorRef, eventBus: EventStream)


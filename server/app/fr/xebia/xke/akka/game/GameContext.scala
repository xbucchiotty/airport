package fr.xebia.xke.akka.game

import akka.actor._
import akka.event.EventStream
import fr.xebia.xke.akka.infrastructure.EventListener
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.airport.Airport

case class GameContext private(listener: ActorRef, game: ActorRef, eventBus: EventStream) {

  def publish(any: AnyRef) {
    eventBus.publish(any)
  }

  def stop(actorSystem: ActorSystem) {
    actorSystem.stop(listener)
    actorSystem.stop(game)
  }

  def init(airTrafficControl: ActorRef, groundControl: ActorRef)(implicit sender: akka.actor.ActorRef) = {
    game ! InitGame(airTrafficControl, groundControl)
  }
}

object GameContext {

  def create(sessionId: String, settings: Settings, planeType: Class[_ <: Plane], airport: Airport)(context: ActorContext): GameContext = {
    val eventStream = new EventStream(false)

    val listener = context.actorOf(EventListener.props(eventStream), sessionId + "-listener")

    val game = context.actorOf(SinglePlayerGame.props(settings, planeType, eventStream, airport), sessionId)

    GameContext(listener, game, eventStream)
  }
}
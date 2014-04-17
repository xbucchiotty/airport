package fr.xebia.xke.akka.game

import akka.actor._
import akka.event.EventStream
import fr.xebia.xke.akka.infrastructure.{SessionId, EventListener}
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.airport.Airport
import fr.xebia.xke.akka.game.SinglePlayerGame.InitGame

case class GameContext(
                        sessionId: SessionId,
                        settings: Settings,
                        airport: Airport,
                        listener: ActorRef,
                        game: ActorRef,
                        eventBus: EventStream) {

  def publish(any: AnyRef) {
    eventBus.publish(any)
  }

  def init(airTrafficControl: ActorRef)(implicit sender: akka.actor.ActorRef) = {
    game ! InitGame(airTrafficControl)
  }

}

object GameContext {

  def create(settings: Settings, planeType: Class[_ <: Plane], airport: Airport, airportClusterLocation: ActorRef)(context: ActorContext): GameContext = {
    val sessionId = SessionId()

    val eventStream = new EventStream(false)

    val listener = context.actorOf(EventListener.props(eventStream), s"$sessionId-listener")

    val game = context.actorOf(SinglePlayerGame.props(sessionId, settings, planeType, eventStream, airport, airportClusterLocation), s"$sessionId-game")

    GameContext(sessionId, settings, airport, listener, game, eventStream)
  }
}
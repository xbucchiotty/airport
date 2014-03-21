package fr.xebia.xke.akka.airport.game

import akka.actor._
import akka.event.EventStream
import fr.xebia.xke.akka.airport.{InitGame, Settings, Game}
import fr.xebia.xke.akka.airport.plane.Plane

case class GameContext private(listener: ActorRef, game: ActorRef, eventBus: EventStream) {

  def publish(any: AnyRef) {
    eventBus.publish(any)
  }

  def stop(actorSystem: ActorSystem) {
    actorSystem.stop(listener)
    actorSystem.stop(game)
  }

  def init(airTrafficControl: ActorSelection, groundControl: ActorSelection)(implicit sender: akka.actor.ActorRef) = {
    game ! InitGame(airTrafficControl, groundControl)
  }
}

object GameContext {

  def create(sessionId: String, settings: Settings, planeType: Class[_ <: Plane])(context: ActorContext): GameContext = {
    val eventStream = new EventStream(false)

    val listener = context.actorOf(EventListener.props(eventStream), sessionId + "-listener")

    val game = context.actorOf(Props(classOf[Game], settings, planeType, eventStream), sessionId)

    GameContext(listener, game, eventStream)
  }
}
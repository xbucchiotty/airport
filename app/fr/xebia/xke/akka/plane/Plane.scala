package fr.xebia.xke.akka.plane

import akka.actor.{ActorLogging, Actor}
import fr.xebia.xke.akka.{Transition, StateMachine}

trait Plane extends Actor with StateMachine with ActorLogging {

  def initialState: State

  def receive: Receive = PartialFunction.empty[Any, Unit]

  override def preStart() {
    transitionTo(() => ())(initialState)
  }

}

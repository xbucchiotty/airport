package fr.xebia.xke.akka.airport.plane

import akka.actor.{ActorLogging, Actor}
import fr.xebia.xke.akka.airport.plane.state.{Transition, StateMachine}

trait Plane extends Actor with StateMachine with ActorLogging{

  def initialState: State

  def initAction: Transition

  def receive: Receive = PartialFunction.empty[Any, Unit]

  override def preStart() {
    transitionTo(initAction)(initialState)
  }

}

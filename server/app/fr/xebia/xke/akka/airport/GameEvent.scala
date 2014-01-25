package fr.xebia.xke.akka.airport

import akka.actor.{Address, ActorSelection}

trait GameEvent

case class Score(current: Int, objective: Int) extends GameEvent

case object GameOver extends GameEvent

case object GameEnd extends GameEvent

case class PlayerUp(address: Address) extends GameEvent

case class PlayerDown(address: Address) extends GameEvent

case class GameStart(airTrafficControlLookup: ActorSelection, groundControlLookup: ActorSelection) extends GameEvent
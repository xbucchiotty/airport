package fr.xebia.xke.akka.game

import akka.actor.{ActorRef, Address}
import fr.xebia.xke.akka.infrastructure.SessionId

trait GameEvent

case class Score(current: Int, objective: Int) extends GameEvent

case object GameOver extends GameEvent

case object GameEnd extends GameEvent

case class PlayerUp(sessionId: SessionId, address: Address) extends GameEvent

case class PlayerDown(sessionId: SessionId, address: Address) extends GameEvent

case class InitGame(airTrafficControl: ActorRef, groundControl: ActorRef) extends GameEvent
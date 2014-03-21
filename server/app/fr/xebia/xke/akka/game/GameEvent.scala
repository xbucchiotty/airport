package fr.xebia.xke.akka.game

import akka.actor.{Address, ActorSelection}
import fr.xebia.xke.akka.infrastructure.TeamMail

trait GameEvent

case class Score(current: Int, objective: Int) extends GameEvent

case object GameOver extends GameEvent

case object GameEnd extends GameEvent

case class PlayerUp(userId: TeamMail, address: Address) extends GameEvent

case class PlayerDown(userId: TeamMail, address: Address) extends GameEvent

case class InitGame(airTrafficControlLookup: ActorSelection, groundControlLookup: ActorSelection) extends GameEvent
package fr.xebia.xke.akka.airport

import akka.actor.{Address, ActorSelection}
import controllers.TeamMail

trait GameEvent

case class Score(current: Int, objective: Int) extends GameEvent

case object GameOver extends GameEvent

case object GameEnd extends GameEvent

case class PlayerUp(userId: TeamMail, address: Address) extends GameEvent

case class PlayerDown(userId: TeamMail, address: Address) extends GameEvent

case class InitGame(airTrafficControlLookup: ActorSelection, groundControlLookup: ActorSelection) extends GameEvent
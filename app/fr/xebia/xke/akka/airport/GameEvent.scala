package fr.xebia.xke.akka.airport

trait GameEvent

case class Score(current: Int, objective: Int) extends GameEvent

case object GameOver extends GameEvent

case object GameEnd extends GameEvent
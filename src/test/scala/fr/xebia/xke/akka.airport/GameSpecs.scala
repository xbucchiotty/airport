package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem, ActorRef}
import org.scalatest.FreeSpec

trait GameSpecs extends FreeSpec {

  def `Given a game`(airControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a game" - {
      fun {
        system.actorOf(Props.create(classOf[Game], airControl), "game")
      }
    }
  }
}

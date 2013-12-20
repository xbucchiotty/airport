package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import fr.xebia.xke.akka.airport._
import org.scalatest.FreeSpec

trait RunwaySpecs extends FreeSpec {


  def `Given a runway`(airControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a runway" - {
      fun {
        system.actorOf(Props(classOf[Runway], airControl), "runway")
      }
    }
  }
}

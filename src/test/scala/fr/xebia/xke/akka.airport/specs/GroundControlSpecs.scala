package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Command.Park
import fr.xebia.xke.akka.airport.Event.Incoming
import fr.xebia.xke.akka.airport._

trait GroundControlSpecs extends ActorSpecs {

  def `Given a ground control`(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a ground control" - {
      fun {
        system.actorOf(Props[GroundControl])
      }
    }
  }

  def `When a plane incomes`(plane: TestProbe, control: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane incomes" - {
      plane.send(control, Incoming)
      fun
    }
  }

  def `Then it should tell the plane to park`(plane: TestProbe) {
    "Then it should tell the plane to park" in {
      plane expectMsgClass classOf[Park]
    }
  }
}

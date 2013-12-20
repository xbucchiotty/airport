package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.Landed
import fr.xebia.xke.akka.airport.Event.Landed
import fr.xebia.xke.akka.airport.Event.{Landed, Incoming}
import fr.xebia.xke.akka.airport._

trait AirTrafficControlSpecs extends ActorSpecs {

  def `Given an air traffic control`(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given an air traffic control" - {
      val airControl = system.actorOf(Props[AirTrafficControl], "aircontrol")

      fun(airControl)
    }

  }

  def `When a plane incomes`(control: ActorRef)(fun: (TestProbe) => NextStep)(implicit system: ActorSystem) {
    "When a plane incomes" - {

      `Given a probe` {
        plane =>
          plane.send(control, Incoming)
          fun(plane)
      }
    }
  }

  def `Then it should tell the plane to land`(plane: TestProbe) {
    "Then it should tell the plane to land" in {

      plane expectMsgAnyClassOf classOf[Land]
    }
  }

  def `Then air traffic control is notified of the landing`(airControl: TestProbe, plane: ActorRef) {
    "Then the air traffic control is notified of the landing" in {
      airControl expectMsg Landed(plane)
    }
  }
}

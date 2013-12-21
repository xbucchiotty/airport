package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Command.{Contact, Land}
import fr.xebia.xke.akka.airport.Event.{Incoming, Landed}
import fr.xebia.xke.akka.airport._

trait AirTrafficControlSpecs extends ActorSpecs {

  def `Given an air traffic control`(groundControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given an air traffic control" - {
      val airControl = system.actorOf(Props(classOf[AirTrafficControl], groundControl), "aircontrol")

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

  def `Then air traffic control is notified of the landing`(airControl: TestProbe, plane: ActorRef, runway: ActorRef) {
    "Then the air traffic control is notified of the landing" in {
      airControl expectMsg Landed(plane, runway)
    }
  }

  def `Then air traffic control should tell the plane to contact ground control`(plane: TestProbe, groundControl: ActorRef) {
    "Then air traffic control should tell the plane to contact ground control" in {
      plane expectMsg Contact(groundControl)
    }
  }


}
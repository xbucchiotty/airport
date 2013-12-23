package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import fr.xebia.xke.akka.airport.NextStep
import fr.xebia.xke.akka.airport.Gate
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Event.HasParked

trait GateSpecs extends ActorSpecs {

  def `Given a gate`(groundControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a gate" - {

      fun {
        system.actorOf(Props(classOf[Gate], groundControl), "gate")
      }
    }
  }

  def `When a plane parks at`(plane: TestProbe, gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane parks " - {
      plane send(gate, HasParked(plane.ref, gate))
      fun
    }
  }

  def `Given a plane is already parked at`(gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "Given a plane is already parked" - {
      `Given a probe` {
        plane =>
          plane send(gate, HasParked(plane.ref, gate))
          fun
      }
    }
  }

}

package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Event.Parked
import org.scalatest.FreeSpec

trait GateSpecs extends FreeSpec {

  def `Given a gate`(fun: ((ActorRef, TestProbe) => Unit))(implicit system: ActorSystem) {
    "Given a gate" - {

      val gate = system.actorOf(Props[Gate], "gate")

      val probe = TestProbe()
      probe watch gate

      fun(gate, probe)
    }
  }

  def `Given a plane has already parked at`(gate: ActorRef)(fun: => Unit)(implicit system: ActorSystem) {
    "Given a plane has already parked" - {

      TestProbe().send(gate, Parked)

      fun
    }
  }


}

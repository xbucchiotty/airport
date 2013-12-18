package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.Parked
import languageFeature.postfixOps
import org.scalatest.{BeforeAndAfter, FunSpec}

class GateSpec extends FunSpec with BeforeAndAfter {

  private implicit val system = ActorSystem.create("GateSpec")

  private val gate = system.actorOf(Props[Gate], "gate")

  describe("A gate") {

    describe("when free") {

      it("can accept a plane") {
        val plane = TestProbe()

        plane.send(gate, Parked)

        plane.expectNoMsg(10 millisecond)
      }
    }

    describe("when occupied") {

      it("can't accept a plane") {
        val plane1 = TestProbe()
        val plane2 = TestProbe()

        val probe = TestProbe()

        probe watch gate

        plane1.send(gate, Parked)
        plane2.send(gate, Parked)

        probe.expectTerminated(gate, 10 milliseconds)

      }
    }

  }
}



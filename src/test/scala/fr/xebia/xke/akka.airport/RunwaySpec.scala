package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.Landed
import org.scalatest.{BeforeAndAfter, FunSpec}

class RunwaySpec extends FunSpec with BeforeAndAfter {

  private implicit var system: ActorSystem = null
  private implicit var runway: ActorRef = null

  private var first: ActorRef = null
  private var second: ActorRef = null

  describe("A runway") {

    it("should accept a plane for landing when free") {
      val probe = TestProbe()
      probe watch runway

      probe.send(runway, Landed(first))

      probe.expectNoMsg(10 millisecond)
    }

    it("shouldn't accept a plane when already occupied") {
      val probe = TestProbe()
      probe watch runway

      probe.send(runway, Landed(first))
      probe.send(runway, Landed(second))

      probe.expectTerminated(runway, 10 millisecond)

    }

  }

  before {
    system = ActorSystem.create("RunwaySpec")
    runway = system.actorOf(Props[Runway])
    first = TestProbe().ref
    second = TestProbe().ref
  }

  after {
    system.shutdown()
  }

}






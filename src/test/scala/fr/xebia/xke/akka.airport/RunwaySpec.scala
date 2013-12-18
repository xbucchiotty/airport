package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Message.Landing
import org.scalatest.{BeforeAndAfter, FunSpec}

class RunwaySpec extends FunSpec with BeforeAndAfter {

  private implicit var system: ActorSystem = null
  private implicit var runway: ActorRef = null

  private val first = new Plane()
  private val second = new Plane()

  describe("A runway") {

    it("should accept a plane for landing when free") {
      val probe = TestProbe()
      probe watch runway

      probe.send(runway, Landing(first))

      probe.expectNoMsg(10 millisecond)
    }

    it("shouldn't accept a plane when already occupied") {
      val probe = TestProbe()
      probe watch runway

      probe.send(runway, Landing(first))
      probe.send(runway, Landing(second))

      probe.expectTerminated(runway, 10 millisecond)

    }

  }

  before {
    system = ActorSystem.create("RunwaySpec")
    runway = system.actorOf(Props[Runway])
  }

  after {
    system.shutdown()
  }

}






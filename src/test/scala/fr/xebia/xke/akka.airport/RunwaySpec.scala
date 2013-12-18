package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.Landed
import org.scalatest.FunSpec

class RunwaySpec extends FunSpec {

  private implicit val system = ActorSystem.create("RunwaySpec")
  private val runway = system.actorOf(Props[Runway], "runway")

  describe("A runway") {

    it("should accept a plane for landing when free") {
      val probe = TestProbe()
      val plane = TestProbe()

      probe watch runway

      plane.send(runway, Landed)

      probe.expectNoMsg(10 millisecond)
    }

    it("shouldn't accept a plane when already occupied") {
      val probe = TestProbe()
      val plane1 = TestProbe()
      val plane2 = TestProbe()

      probe watch runway

      plane1.send(runway, Landed)
      plane2.send(runway, Landed)

      probe.expectTerminated(runway, 10 millisecond)

    }

  }

}






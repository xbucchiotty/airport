package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.Landed
import org.scalatest.{OneInstancePerTest, BeforeAndAfter, FunSpec}

class RunwaySpec extends FunSpec with OneInstancePerTest with BeforeAndAfter {

  private implicit val system = ActorSystem.create("RunwaySpec")
  private val airControl = TestProbe()

  describe("A runway") {
    val runway = system.actorOf(Props(classOf[Runway], airControl.ref), "runway")

    it("should accept a plane for landing when free") {
      val probe = TestProbe()
      val plane = TestProbe()

      probe watch runway

      plane.send(runway, Landed(plane.ref))

      probe.expectNoMsg(10 millisecond)
      plane.expectNoMsg(10 millisecond)
    }

    it("shouldn't accept a plane when already occupied") {
      val probe = TestProbe()
      val plane1 = TestProbe()
      val plane2 = TestProbe()

      probe watch runway

      plane1.send(runway, Landed(plane1.ref))
      plane2.send(runway, Landed(plane2.ref))

      probe.expectTerminated(runway, 10 millisecond)

    }

    it("should notify air control when plane has landed") {
      val plane = TestProbe()
      val planeRef = plane.ref

      val msg = Landed(planeRef)
      plane.send(runway, msg)

      airControl.expectMsg(msg)
    }

  }

  after {
    system.shutdown()
  }
}
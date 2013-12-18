package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.Landed
import org.scalatest.FunSpec

class RunwaySpec extends FunSpec {

  private implicit val system = ActorSystem.create("RunwaySpec")
  private val runway = system.actorOf(Props[Runway])

  private val first = TestProbe().ref
  private val second = TestProbe().ref

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

}






package fr.xebia.xke.akka.airport

import scala.language.postfixOps
import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Land
import org.scalatest.{BeforeAndAfter, FunSpec}
import fr.xebia.xke.akka.airport.Event.Landed

class PlaneSpec extends FunSpec with BeforeAndAfter {

  private implicit var system: ActorSystem = null
  private implicit var flyingPlane: ActorRef = null

  describe("A plane") {
    describe("in the air") {
      it("can lands on a runway") {
        val control = TestProbe()
        val runway = TestProbe()

        control.send(flyingPlane, Land(runway.ref))

        runway.expectMsg(Plane.MAX_LANDING_TIMEOUT milliseconds, Landed(flyingPlane))
      }
    }
  }

  before {
    system = ActorSystem.create("PlaneSpec")
    flyingPlane = system.actorOf(Props(classOf[Plane], true))
  }

  after {
    system.shutdown()
  }
}

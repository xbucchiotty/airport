package fr.xebia.xke.akka.airport

import Plane.MAX_LANDING_TIMEOUT
import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.Landed
import org.scalatest.FunSpec
import scala.language.postfixOps

class PlaneSpec extends FunSpec {

  private implicit val system: ActorSystem = ActorSystem.create("PlaneSpec")

  describe("A plane") {

    describe("in the air") {

      val flyingPlane = system.actorOf(Props[Plane])

      it("can lands on a runway") {
        val control = TestProbe()
        val runway = TestProbe()

        control.send(flyingPlane, Land(runway.ref))

        runway.expectMsg(MAX_LANDING_TIMEOUT milliseconds, Landed(flyingPlane))
      }
    }
  }

}

package fr.xebia.xke.akka.airport

import Plane.MAX_LANDING_TIMEOUT
import akka.actor.{Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Land
import org.scalatest.FunSpec
import scala.language.postfixOps
import fr.xebia.xke.akka.airport.Event.Landed

class PlaneSpec extends FunSpec {

  private implicit val system: ActorSystem = ActorSystem.create("PlaneSpec")

  describe("A plane") {

    describe("when flying") {

      val flyingPlane = system.actorOf(Props[Plane],"plane")

      it("can lands on a runway") {
        val control = TestProbe()
        val runway = TestProbe()

        control.send(flyingPlane, Land(runway.ref))

        runway.expectMsg(MAX_LANDING_TIMEOUT milliseconds, Landed)
      }
    }
  }

}

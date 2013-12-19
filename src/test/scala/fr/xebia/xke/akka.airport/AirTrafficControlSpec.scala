package fr.xebia.xke.akka.airport

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.Incoming
import languageFeature.postfixOps
import org.scalatest.{BeforeAndAfter, OneInstancePerTest, FunSpec}

class AirTrafficControlSpec extends FunSpec with OneInstancePerTest with BeforeAndAfter {

  private implicit val system = ActorSystem.create("ControlSpec")
  private val airControl = system.actorOf(Props[AirTrafficControl], "runway")
  private val plane = TestProbe()

  describe("An air traffic control") {

    Given_a_free_runway {

      When_a_plane_incomes {

        it("should tell the plane to land") {

          plane.expectMsgAnyClassOf(classOf[Land])

        }
      }
    }
  }


  def Given_a_free_runway(fun: => Unit) =
    describe("given a runway is free") {
      fun
    }

  def When_a_plane_incomes(fun: => Unit) =
    describe("when a new plane incomes") {

      plane.send(airControl, Incoming)

      fun
    }


  after {
    system.shutdown()
  }
}


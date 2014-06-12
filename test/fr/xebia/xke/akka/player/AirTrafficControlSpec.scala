package fr.xebia.xke.akka.player

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.message.{AirTrafficControlReady, InitAirTrafficControl, PlaneEvent}
import PlaneEvent.{HasLeft, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.message.command.Land
import language.postfixOps
import org.scalatest.{BeforeAndAfterEach, ShouldMatchers, GivenWhenThen, FunSpec}
import com.typesafe.config.ConfigFactory
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack

class AirTrafficControlSpec extends FunSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterEach {

  describe("An air traffic control") {

    it("can be initialized") {
      val runway = TestProbe()
      val groundControl = TestProbe()
      val airTrafficControl = system.actorOf(Props[AirTrafficControl], "airTrafficControl")

      val game = TestProbe()
      game.send(airTrafficControl, InitAirTrafficControl(groundControl.ref, Set(runway.ref), 100))

      game expectMsg AirTrafficControlReady
    }

    it("should tell the plane to land when there is a free runway") {
      pending

      Given("an air traffic control with 1 runway")
      val runway = TestProbe()
      val groundControl = TestProbe()
      val airTrafficControl = initializedAirTrafficControl(groundControl.ref, Set(runway.ref), 100)

      When("a new plane incomes")
      val plane = TestProbe()
      plane.send(airTrafficControl, Incoming)

      Then("air traffic control should tell the plane to land on the free runway")
      plane.expectMsg(max = 100 milliseconds, Land(runway.ref))
      plane reply Ack
    }

    it("should alternate the runway allocated to a new incoming plane") {
      pending

      Given("an air traffic control with 2 runways")
      val runway1 = TestProbe()
      val runway2 = TestProbe()
      val groundControl = TestProbe()
      val airTrafficControl = initializedAirTrafficControl(groundControl.ref, Set(runway1.ref, runway2.ref), 100)

      When("a new 2 planes income")
      val plane1 = TestProbe()
      plane1.send(airTrafficControl, Incoming)
      val plane2 = TestProbe()
      plane2.send(airTrafficControl, Incoming)

      Then("air traffic control should allocate one of the two free runways to each plane")
      val allocation1: Land = plane1.expectMsgAllClassOf(classOf[Land]).head
      val allocation2: Land = plane2.expectMsgAllClassOf(classOf[Land]).head

      plane1 reply Ack
      plane2 reply Ack

      allocation1.runway should (equal(runway1.ref) or equal(runway2.ref))
      allocation2.runway should (equal(runway1.ref) or equal(runway2.ref))

      allocation1.runway should not equal allocation2.runway
    }

    it("should not tell the plane to land until a runway is free") {
      pending

      Given("an air traffic control with 1 runway")
      val runway = TestProbe()
      val groundControl = TestProbe()

      val airTrafficControl = initializedAirTrafficControl(groundControl.ref, Set(runway.ref), 100)

      Given("a first plane has landed on the runway")
      val firstPlane = TestProbe()
      firstPlane.send(airTrafficControl, Incoming)
      firstPlane.expectMsg(100 milliseconds, Land(runway.ref))
      firstPlane reply Ack
      firstPlane reply HasLanded

      When("when a new plane incomes")
      val plane = TestProbe()
      plane.send(airTrafficControl, Incoming)

      Then("air traffic control not should tell anything to the plane")
      plane.expectNoMsg(100 milliseconds)

      When("first plane leaves")
      firstPlane.send(airTrafficControl, HasLeft)

      Then("air traffic control should tell the plane to land on the free runway")
      plane.expectMsg(max = 100 milliseconds, Land(runway.ref))
      plane reply Ack
    }

    it("should repeat message until it's successfully received") {
      pending
      Given("an air traffic control with 1 runway")
      val runway = TestProbe()
      val groundControl = TestProbe()
      val airTrafficControl = initializedAirTrafficControl(groundControl.ref, Set(runway.ref), 100)

      When("when a new plane incomes")
      val plane = TestProbe()
      plane.send(airTrafficControl, Incoming)

      Then("air traffic control should repeat the order until it's successfully acked by plane")
      plane.expectMsg(max = 150.milliseconds, Land(runway.ref))
      plane.expectMsg(max = 150.milliseconds, Land(runway.ref))
      plane.expectMsg(max = 150.milliseconds, Land(runway.ref))

      plane.reply(Ack)
      plane.expectNoMsg(150.milliseconds)

    }
  }

  implicit var system: ActorSystem = _

  override protected def afterEach(): Unit = {
    system.shutdown()
    system.awaitTermination()
  }

  override protected def beforeEach(): Unit = {
    system = {
      ActorSystem("TestSystem", ConfigFactory.load("application-test.conf").getConfig("player"))
    }
  }

  def initializedAirTrafficControl(groundControl: ActorRef, runways: Set[ActorRef], ackMaxTimout: Int)(implicit system: ActorSystem): ActorRef = {
    val airTrafficControl = system.actorOf(Props[AirTrafficControl], "airTrafficControl")
    val game = TestProbe()
    game.send(airTrafficControl, InitAirTrafficControl(groundControl, runways, ackMaxTimout))
    game expectMsg AirTrafficControlReady

    airTrafficControl
  }

  }
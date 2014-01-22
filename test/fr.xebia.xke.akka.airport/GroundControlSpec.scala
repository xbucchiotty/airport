package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent.{EndOfTaxi, HasLeft, HasParked, Incoming}
import language.postfixOps
import org.scalatest._
import fr.xebia.xke.akka.airport.command.{ParkAt, Ack, Taxi}

class GroundControlSpec extends FunSpec with GivenWhenThen with ShouldMatchers {

  describe("A ground control in taxiway management") {

    it("should tell the plane to taxi when 1 taxiway is free") {
      pending

      Given("a ground control with 1 taxiway")
      implicit val system = ActorSystem()
      val taxiway = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq(taxiway.ref), Seq.empty[ActorRef], 1, 100), "groundControl")

      When("a new plane incomes")
      val plane = TestProbe()
      plane.send(groundControl, Incoming)

      Then("ground control should tell the plane to taxi on the free taxiway")
      plane expectMsg(100 milliseconds, Taxi(taxiway.ref))
      plane reply Ack
    }

    it("should not tell the plane to taxi until 1 taxiway is free") {
      pending

      Given("a ground control with 1 taxiway")
      implicit val system = ActorSystem()
      val taxiway = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq(taxiway.ref), Seq.empty[ActorRef], 1, 100), "groundControl")

      Given("a plane is already on the taxiway")
      val firstPlane = TestProbe()
      firstPlane.send(groundControl, Incoming)
      firstPlane.expectMsg(100 milliseconds, Taxi(taxiway.ref))
      firstPlane.reply(Ack)

      When("a new plane incomes")
      val plane = TestProbe()
      plane.send(groundControl, Incoming)

      Then("ground control should not tell anything to the plane")
      plane expectNoMsg (100 milliseconds)

      When("First plane leaves the taxiway")
      firstPlane.send(groundControl, HasParked)

      Then("ground control should tell the plane to taxi on the free taxiway")
      plane.expectMsg(100 milliseconds, Taxi(taxiway.ref))
      plane reply Ack
    }

    it("should fill taxiway until it's full") {
      pending

      Given("a ground control with 1 taxiway with a capacity of 4 planes")
      implicit val system = ActorSystem()
      val taxiway = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq(taxiway.ref), Seq.empty[ActorRef], 4, 100), "groundControl")

      Given("4 planes")
      val planes = List.fill(4)(TestProbe())

      When("each plane incomes")
      planes.foreach(_.send(groundControl, Incoming))

      Then("ground control should tell each plane to taxi on the taxiway")
      planes.foreach(plane => {
        plane.expectMsg(100 milliseconds, Taxi(taxiway.ref))
        plane reply Ack
      })

      When("a new plane incomes")
      val newPlane = TestProbe()
      newPlane.send(groundControl, Incoming)

      Then("ground control should not tell anything to the plane")
      newPlane expectNoMsg (100 milliseconds)
    }

    it("should allocate free taxiways to each plane") {
      pending

      Given("a ground control with 4 taxiways with a capacity of 1 plane")
      implicit val system = ActorSystem()
      val taxiways = List.fill(4)(TestProbe()).map(_.ref).toSeq
      val groundControl = system.actorOf(Props(classOf[GroundControl], taxiways, Seq.empty[ActorRef], 1, 100), "groundControl")

      Given("4 planes")
      val planes = List.fill(4)(TestProbe())

      When("each plane incomes")
      planes.foreach(_.send(groundControl, Incoming))

      Then("ground control should tell each plane to taxi on the taxiway")
      val replies = planes.map {
        plane =>
          val msg = plane.expectMsgAllClassOf(100 milliseconds, classOf[Taxi]).head
          plane reply Ack
          msg
      }

      replies.foreach(allocatedTaxiway => taxiways should contain(allocatedTaxiway.taxiway))
      replies.map(_.taxiway).toSet should have size 4 // 4 different taxiways

      When("a new plane incomes")
      val newPlane = TestProbe()
      newPlane.send(groundControl, Incoming)

      Then("ground control should not tell anything to the plane")
      newPlane expectNoMsg (100 milliseconds)
    }
  }

  describe("A ground control in gate management") {

    it("should tell the plane to park when there is a free gate") {
      Given("a ground control with 1 gate")
      implicit val system = ActorSystem()
      val gate = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq.empty[ActorRef], Seq(gate.ref), 0, 100), "groundControl")

      When("a new plane requests to park")
      val plane = TestProbe()
      plane.send(groundControl, EndOfTaxi)

      Then("ground control should tell the plane to park on the free gate")
      plane.expectMsg(max = 100 milliseconds, ParkAt(gate.ref))
      plane reply Ack
    }

    it("should alternate the runway allocated to a new incoming plane") {
      pending

      Given("a ground control with 2 gates")
      implicit val system = ActorSystem()
      val gate1 = TestProbe()
      val gate2 = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq.empty[ActorRef], Seq(gate1.ref, gate2.ref), 0, 100), "groundControl")

      When("a new 2 request to park")
      val plane1 = TestProbe()
      plane1.send(groundControl, EndOfTaxi)
      val plane2 = TestProbe()
      plane2.send(groundControl, EndOfTaxi)

      Then("ground control should allocate one of the two free gates to each plane")
      val allocation1 = plane1.expectMsgAllClassOf(150.milliseconds, classOf[ParkAt]).head
      val allocation2 = plane2.expectMsgAllClassOf(150.milliseconds, classOf[ParkAt]).head

      plane1 reply Ack
      plane2 reply Ack

      allocation1.gate should (equal(gate1.ref) or equal(gate2.ref))
      allocation2.gate should (equal(gate1.ref) or equal(gate2.ref))

      allocation1.gate should not equal allocation2.gate
    }

    it("should not tell the plane to park until a gate is free") {
      pending

      Given("a ground control with 1 gate")
      implicit val system = ActorSystem()
      val gate = TestProbe()
      val taxiway = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq(taxiway.ref), Seq(gate.ref), 1, 100), "groundControl")

      Given("a first plane has parked at the gate")
      val firstPlane = TestProbe()
      firstPlane.send(groundControl, Incoming)
      firstPlane.expectMsg(100 milliseconds, Taxi(taxiway.ref))
      firstPlane reply Ack
      firstPlane.send(groundControl, EndOfTaxi)
      firstPlane.expectMsg(100 milliseconds, ParkAt(gate.ref))
      firstPlane reply Ack
      firstPlane.send(groundControl, HasParked)

      When("when a new plane requests to park")
      val plane = TestProbe()
      plane.send(groundControl, EndOfTaxi)

      Then("ground control not should tell anything to the plane")
      plane.expectNoMsg(100 milliseconds)

      When("first plane leaves")
      firstPlane.send(groundControl, HasLeft)

      Then("ground control should tell the plane to park at the free gate")
      plane.expectMsg(max = 100 milliseconds, ParkAt(gate.ref))
      plane reply Ack
    }

    it("should repeat message until it's successfully received") {
      pending

      Given("an air traffic control with 1 runway")
      implicit val system = ActorSystem()
      val gate = TestProbe()
      val taxiway = TestProbe()
      val groundControl = system.actorOf(Props(classOf[GroundControl], Seq(taxiway.ref), Seq(gate.ref), 1, 100), "groundControl")

      When("a new plane incomes")
      val plane = TestProbe()
      plane.send(groundControl, Incoming)

      Then("ground control should repeat the order until it's successfully acked by plane")
      plane expectMsg(150.milliseconds, Taxi(taxiway.ref))
      plane expectMsg(150.milliseconds, Taxi(taxiway.ref))
      plane expectMsg(150.milliseconds, Taxi(taxiway.ref))
      plane.reply(Ack)
      plane.expectNoMsg(150.milliseconds)

      plane.send(groundControl, EndOfTaxi)
      plane expectMsg(150.milliseconds, ParkAt(gate.ref))
      plane expectMsg(150.milliseconds, ParkAt(gate.ref))
      plane expectMsg(150.milliseconds, ParkAt(gate.ref))
      plane.reply(Ack)
      plane.expectNoMsg(150.milliseconds)
    }
  }
}
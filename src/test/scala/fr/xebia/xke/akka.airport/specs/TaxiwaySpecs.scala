package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Event.HasEntered
import fr.xebia.xke.akka.airport._
import org.scalatest.FreeSpec

trait TaxiwaySpecs extends FreeSpec {

  def `Given a taxiway of capacity`(capacity: Int, groundControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    s"Given a taxiway of capacity $capacity" - {
      fun {
        system.actorOf(Props(classOf[Taxiway], 1, groundControl), "taxiway")
      }
    }
  }

  def `Then ground control is notified of the plane entering the taxiway`(groundControl: TestProbe, plane: ActorRef, taxiway: ActorRef) {
    "Then ground control is notified of the plane entering the taxiway" in {
      groundControl expectMsg HasEntered(plane, taxiway)
    }
  }

  def `When a plane enters the taxiway`(plane: TestProbe, taxiway: ActorRef)(fun: => NextStep) {
    "When a plane enters the taxiway" - {
      plane send(taxiway, HasEntered(plane.ref, taxiway))
      fun
    }
  }

  def `Given a plane has already entered the taxiway`(taxiway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "Given a plane has already entered the taxiway" - {
      val plane = TestProbe()

      plane send(taxiway, HasEntered(plane.ref, taxiway))
      fun
    }
  }
}

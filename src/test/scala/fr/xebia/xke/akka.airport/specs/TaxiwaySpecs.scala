package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.{TaxiingToGate, HasParked, HasLeft, HasEntered}
import fr.xebia.xke.akka.airport.{NextStep, Taxiway}
import languageFeature.postfixOps
import org.scalatest.FreeSpec

trait TaxiwaySpecs extends FreeSpec {

  def `Given a taxiway of capacity`(capacity: Int, groundControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    s"Given a taxiway of capacity $capacity" - {
      fun {
        system.actorOf(Props(classOf[Taxiway], capacity, groundControl), "taxiway")
      }
    }
  }

  def `When a plane enters the taxiway`(plane: TestProbe, taxiway: ActorRef, gate: ActorRef)(fun: => NextStep) {
    s"When a plane ${plane.ref.path.name} enters the taxiway" - {
      plane send(taxiway, TaxiingToGate(plane.ref, taxiway, gate))
      fun
    }
  }

  def `When queuing timeout is reached`(taxiway: ActorRef, plane: ActorRef, gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When queuing timeout is reached" - {
      TestProbe().send(taxiway, HasParked(plane, gate))
      fun
    }
  }

  def `Then ground control is notified of the plane leaving the taxiway`(groundControl: TestProbe, plane: ActorRef, taxiway: ActorRef) {
    "Then ground control is notified of the plane leaving the taxiway" in {
      groundControl.fishForMessage(max = (2 * Taxiway.TAXIING_TIMEOUT).milliseconds) {
        case HasLeft(planeFromMsg, taxiwayFromMsg) =>
          plane === planeFromMsg && taxiway === taxiwayFromMsg
      }
    }
  }

  def `Then ground control is notified of the plane entering the taxiway`(groundControl: TestProbe, plane: ActorRef, taxiway: ActorRef) {
    "Then ground control is notified of the plane entering the taxiway" in {
      groundControl.fishForMessage(max = (2 * Taxiway.TAXIING_TIMEOUT).milliseconds) {
        case HasEntered(planeFromMsg, taxiwayFromMsg) =>
          plane === planeFromMsg && taxiway === taxiwayFromMsg
      }
    }
  }

  def `Then plane should be out of taxiway within timeout`(taxiway: ActorRef, groundControl: TestProbe, gate: TestProbe, plane: ActorRef) {
    "Then plane should be out of taxiway within timeout" in {
      gate.fishForMessage(max = (2 * Taxiway.TAXIING_TIMEOUT).milliseconds) {
        case HasParked(planeFromMsg, gateFromMsg) =>
          plane === planeFromMsg && gate.ref === gateFromMsg
      }
    }
  }

}

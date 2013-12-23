package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.{HasParked, HasLeft, HasEntered, HasLanded}
import fr.xebia.xke.akka.airport.{Command, Plane, NextStep}
import languageFeature.postfixOps

trait PlaneSpecs extends ActorSpecs {

  def `Given a flying plane`(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a flying plane" - {
      fun {
        system.actorOf(Props[Plane], "plane")
      }
    }
  }

  def `When a plane is requested to land`(airControl: TestProbe, plane: ActorRef, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane is requested to land" - {
      airControl send(plane, Land(runway))
      fun
    }
  }

  def `When a plane is requested to taxi to gate through taxiway`(groundControl: TestProbe, plane: ActorRef, taxiway: ActorRef, gate: ActorRef)(fun: => NextStep) {
    "When a plane is requested to taxi to gate through taxiway" - {
      groundControl send(plane, Command.TaxiAndPark(taxiway, gate))
      fun
    }
  }

  def `Given a plane has already landed`(plane: ActorRef, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "Given the plane has already landed" - {
      TestProbe().send(plane, HasLanded(plane, runway))
      fun
    }
  }

  def `When a plane parks at`(plane: ActorRef, gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane parks at" - {
      TestProbe().send(plane, HasParked(plane, gate))
      fun
    }
  }

  def `Then plane should leave runway`(plane: ActorRef, runway: TestProbe) {
    "Then plane should leave runway" in {
      runway expectMsg HasLeft(plane, runway.ref)
    }
  }

  def `Then plane should enter the taxiway`(plane: ActorRef, taxiway: TestProbe) {
    "Then plane should enter the taxiway" in {
      taxiway expectMsg HasEntered(plane, taxiway.ref)
    }
  }

  def `Then the plane should land within timeout`(plane: ActorRef, runway: TestProbe) {
    "Then the plane should land within timeout" in {
      runway.expectMsg((Plane.MAX_LANDING_TIMEOUT * 2) milliseconds, HasLanded(plane, runway.ref))
    }
  }

}

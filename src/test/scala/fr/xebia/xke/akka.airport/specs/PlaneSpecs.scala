package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.{HasLeft, HasEntered, HasLanded, HasParked}
import fr.xebia.xke.akka.airport.Plane
import fr.xebia.xke.akka.airport.NextStep
import languageFeature.postfixOps
import concurrent.duration._

trait PlaneSpecs extends ActorSpecs {

  def `Given a flying plane`(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a flying plane" - {
      fun {
        system.actorOf(Props[Plane])
      }
    }
  }

  def `Given a plane has already parked at`(gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "Given a plane has already parked" - {
      `Given a probe` {
        plane =>
          plane send(gate, HasParked(plane.ref, gate))
          fun
      }
    }
  }

  def `When a plane parks at`(gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane parks " - {
      `Given a probe` {
        plane =>
          plane send(gate, HasParked(plane.ref, gate))
          fun
      }
    }
  }

  def `When a plane is requested to land`(plane: ActorRef, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane is requested to land" - {
      `Given a probe` {
        control =>
          control send(plane, Land(runway))
          fun
      }
    }
  }

  def `Given a plane has already landed`(plane: TestProbe, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane has already landed" - {
      plane send(runway, HasLanded(plane.ref, runway))
      fun
    }
  }

  def `When a plane lands at`(plane: TestProbe, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane lands " - {
      plane send(runway, HasLanded(plane.ref, runway))
      fun
    }
  }

  def `When the plane leaves`(plane: TestProbe, target: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    s"When the plane ${plane.ref.path.name} leaves ${target.path.name}" - {
      plane send(target, HasLeft(plane.ref, target))
      fun
    }
  }

  def `When the plane lands at`(plane: TestProbe, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When the plane lands " - {
      plane send(runway, HasLanded(plane.ref, runway))
      fun
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

  def `Then the plane should land within timeout`(plane: ActorRef, runway: TestProbe) {
    "Then the plane should land within timeout" in {

      runway.expectMsg(Plane.MAX_LANDING_TIMEOUT milliseconds, HasLanded(plane, runway.ref))
    }
  }

}

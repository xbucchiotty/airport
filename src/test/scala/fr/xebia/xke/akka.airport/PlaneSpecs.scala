package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Command.Land
import fr.xebia.xke.akka.airport.Event.{Landed, Parked}
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

  def `When a plane requested to land`(plane: ActorRef, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane requested to land" - {
      `Given a probe` {
        control =>
          control send(plane, Land(runway))
          fun
      }
    }
  }

  def `Given a plane has already landed`(runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane requested to land" - {
      `Given a probe` {
        plane =>
          plane send(runway, Landed(plane.ref))
          fun
      }
    }
  }

  def `Given a plane has already parked at`(gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "Given a plane has already parked" - {
      `Given a probe` {
        probe =>
          probe send(gate, Parked)
          fun
      }
    }
  }

  def `When a plane parks at`(gate: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane parks " - {
      `Given a probe` {
        plane =>
          plane send(gate, Parked)
          fun
      }

    }
  }

  def `When a plane lands at`(runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane parks " - {
      `Given a probe` {
        plane =>
          plane send(runway, Landed(plane.ref))
          fun
      }

    }
  }

  def `Then the plane should land within timeout`(plane: ActorRef, runway: TestProbe) {
    "Then the plane should land within timeout" in {

      runway.expectMsg(Plane.MAX_LANDING_TIMEOUT milliseconds, Landed(plane))
    }
  }

}

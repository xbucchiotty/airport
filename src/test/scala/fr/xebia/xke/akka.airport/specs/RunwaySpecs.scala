package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import fr.xebia.xke.akka.airport._
import org.scalatest.FreeSpec
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Event.{HasLeft, HasLanded}

trait RunwaySpecs extends FreeSpec {


  def `Given a runway`(airControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a runway" - {
      fun {
        system.actorOf(Props(classOf[Runway], airControl), "runway")
      }
    }
  }

  def `Given a plane has already landed`(plane: TestProbe, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane has already landed" - {
      plane send(runway, HasLanded)
      fun
    }
  }

  def `When a plane lands at`(plane: TestProbe, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When a plane lands " - {
      plane send(runway, HasLanded)
      fun
    }
  }

  def `When the plane leaves`(plane: TestProbe, target: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    s"When the plane ${plane.ref.path.name} leaves ${target.path.name}" - {
      plane send(target, HasLeft)
      fun
    }
  }

  def `When the plane lands at`(plane: TestProbe, runway: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    "When the plane lands " - {
      plane send(runway, HasLanded)
      fun
    }
  }

}

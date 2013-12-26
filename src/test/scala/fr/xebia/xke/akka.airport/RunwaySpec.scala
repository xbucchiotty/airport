package fr.xebia.xke.akka.airport

import akka.actor.Props
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.{HasLeft, HasLanded}
import fr.xebia.xke.akka.airport.specs.{ActorSpecs, PlaneSpecs}
import languageFeature.postfixOps

class RunwaySpec extends ActorSpecs with PlaneSpecs {

  `Given an actor system` {
    implicit system =>

      "Given a free runway" - {

        "Then a plane can land on the runway" in {
          val runway = system.actorOf(Props[Runway], "runway")
          val probe = TestProbe()
          probe watch runway

          val plane = TestProbe()

          plane.send(runway, HasLanded)

          probe expectNoMsg (100 milliseconds)

        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given an occupied runway" - {

        "When a plane try to land on the runway" - {

          "Then the runway should terminates" in {
            val runway = system.actorOf(Props[Runway], "runway")
            val probe = TestProbe()
            probe watch runway

            val firstPlane = TestProbe()
            val secondPlane = TestProbe()

            firstPlane.send(runway, HasLanded)
            secondPlane.send(runway, HasLanded)

            probe expectTerminated(runway, 100 milliseconds)
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given an occupied runway" - {

        "When a plane try to leave without having landed before" - {

          "Then the runway should terminates" in {
            val runway = system.actorOf(Props[Runway], "runway")
            val probe = TestProbe()
            probe watch runway

            val plane = TestProbe()

            plane.send(runway, HasLeft)

            probe expectTerminated(runway, 100 milliseconds)
          }
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given an occupied runway" - {

        "When the first plane leaves the runway" - {

          "Then the second plane can land" in {
            val runway = system.actorOf(Props[Runway], "runway")
            val probe = TestProbe()
            probe watch runway

            val firstPlane = TestProbe()
            val secondPlane = TestProbe()

            firstPlane.send(runway, HasLanded)
            firstPlane.send(runway, HasLeft)
            secondPlane.send(runway, HasLanded)

            probe expectNoMsg (100 milliseconds)
          }
        }
      }
  }
}
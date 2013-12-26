package fr.xebia.xke.akka.airport

import akka.actor.Props
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.{TaxiingToGate, HasParked}
import fr.xebia.xke.akka.airport.specs.{PlaneSpecs, ActorSpecs}
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers

class TaxiwaySpec extends PlaneSpecs with ActorSpecs with ShouldMatchers {

  `Given an actor system` {
    implicit system =>

      "Given a taxiway of capacity 1 " - {

        "Given a plane enters the taxiway" - {

          "When queueing is finished" - {

            "Then Gate and planes are notified of the parking event" in {
              val taxiway = system.actorOf(Props(classOf[Taxiway], 1), "taxiway")

              val plane = TestProbe()
              val gate = TestProbe()

              plane send(taxiway, TaxiingToGate(gate.ref))

              plane.expectMsg(Taxiway.TAXIING_TIMEOUT milliseconds, HasParked)
              gate.expectMsg(Taxiway.TAXIING_TIMEOUT milliseconds, HasParked)
            }
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a taxiway of capacity 1 " - {

        "Given a plane is queueing" - {

          "When a second plane try to enter the taxiway" - {

            "The taxiway should terminates" in {
              val taxiway = system.actorOf(Props(classOf[Taxiway], 1), "taxiway")

              val firstPlane = TestProbe()
              val secondPlane = TestProbe()
              val gate = TestProbe()
              val probe = TestProbe()
              probe watch taxiway

              firstPlane send(taxiway, TaxiingToGate(gate.ref))
              secondPlane send(taxiway, TaxiingToGate(gate.ref))

              probe expectTerminated(taxiway, 100 milliseconds)
            }
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a taxiway of capacity 5 " - {

        "When 5 planes are queueing" - {

          "Then they should exit in the same order" in {

            val taxiway = system.actorOf(Props(classOf[Taxiway], 5), "taxiway")

            val planes = for (i <- 0 until 5)
            yield TestProbe()
            val gate = TestProbe()

            planes.foreach(plane => plane send(taxiway, TaxiingToGate(gate.ref)))

            planes.foreach(plane => {
              gate expectMsg(Taxiway.TAXIING_TIMEOUT milliseconds, HasParked)
              gate.lastSender should be(plane.ref)
            })
          }
        }
      }
  }
}
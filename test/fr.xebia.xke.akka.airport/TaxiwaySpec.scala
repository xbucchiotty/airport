package fr.xebia.xke.akka.airport

import akka.actor.Props
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, EndOfTaxi, Taxiing, HasParked}
import fr.xebia.xke.akka.airport.specs.ActorSpecs
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers

class TaxiwaySpec extends ActorSpecs with ShouldMatchers {

  val settings = Settings.TEST

  `Given an actor system` {
    implicit system =>

      "Given a taxiway of capacity 1 " - {

        "Given a plane enters the taxiway" - {

          "When queueing is finished" - {

            "Then plane is notified of the end of parking event" in {
              val taxiway = system.actorOf(Props(classOf[Taxiway], settings), "taxiway")

              val plane = TestProbe()

              plane send(taxiway, Taxiing)

              plane.expectMsg(EndOfTaxi)
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
              val taxiway = system.actorOf(Props(classOf[Taxiway], settings.copy(taxiwayCapacity = 1)), "taxiway")

              val firstPlane = TestProbe()
              val secondPlane = TestProbe()
              val probe = TestProbe()
              probe watch taxiway

              firstPlane send(taxiway, Taxiing)
              secondPlane send(taxiway, Taxiing)

              probe expectTerminated(taxiway, 100 milliseconds)
            }
          }
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given a taxiway of capacity 1 " - {

        "Given a plane is queueing" - {

          "Given a plane has left the taxiway" - {

            "When a second plane try to enter the taxiway" - {

              "The taxiway should terminates" in {
                val taxiway = system.actorOf(Props(classOf[Taxiway], settings.copy(taxiwayCapacity = 1)), "taxiway")

                val firstPlane = TestProbe()
                val secondPlane = TestProbe()
                val probe = TestProbe()
                probe watch taxiway

                firstPlane send(taxiway, Taxiing)

                firstPlane send(taxiway, HasLeft)

                secondPlane send(taxiway, Taxiing)

                secondPlane.expectMsg(EndOfTaxi)
              }
            }
          }
        }
      }
  }
}
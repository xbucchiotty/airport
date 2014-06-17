package fr.xebia.xke.akka.airport

import akka.actor.Props
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.message.PlaneEvent
import PlaneEvent.{HasLeft, HasParked}
import languageFeature.postfixOps
import concurrent.duration._
import fr.xebia.xke.akka.ActorSpecs

class GateSpec extends ActorSpecs {

  `Given an actor system` {
    implicit system =>

      "Given a free gate" - {

        "Given a plane" - {

          "Then a plane car parks at the gate" in {
            val gate = system.actorOf(Gate.props(), "gate")
            val probe = TestProbe()
            probe watch gate

            val plane = TestProbe()

            plane send(gate, HasParked)
            probe expectNoMsg (100 milliseconds)
          }
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given two planes" - {

        "Given a free gate" - {

          "Given first plane is parked at the gate" - {

            "When a second plane try to park" - {

              "Then gate terminates" in {
                val firstPlane = TestProbe()
                val secondPlane = TestProbe()

                val gate = system.actorOf(Gate.props(), "gate")
                val probe = TestProbe()
                probe watch gate

                firstPlane send(gate, HasParked)

                secondPlane send(gate, HasParked)

                probe expectTerminated(gate, 100 milliseconds)
              }
            }
          }
        }
      }
  }
  `Given an actor system` {
    implicit system =>

      "Given two planes" - {

        "Given a free gate" - {

          "When first plane parkes and leaves the gate" - {

            "Then second plane can park at the gate" - {
              val firstPlane = TestProbe()
              val secondPlane = TestProbe()

              val gate = system.actorOf(Gate.props(), "gate")
              val probe = TestProbe()
              probe watch gate

              firstPlane send(gate, HasParked)
              firstPlane send(gate, HasLeft)
              secondPlane send(gate, HasParked)

              probe expectNoMsg (100 milliseconds)

            }
          }
        }
      }
  }
}
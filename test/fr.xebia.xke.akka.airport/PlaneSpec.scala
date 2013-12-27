package fr.xebia.xke.akka.airport

import akka.actor.Props
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Command.Contact
import fr.xebia.xke.akka.airport.GameEvent.{Score, HasParked, TaxiingToGate, HasLeft, StartTaxi, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.specs.ActorSpecs
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers

class PlaneSpec extends ActorSpecs with ShouldMatchers {

  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "When it starts" - {

          "Then it should contact the aircontrol" in {
            val game = TestProbe()
            val airControl = TestProbe()

            system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")

            airControl expectMsg Incoming
          }
        }
      }
  }


  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "When it runs out of kerozen" - {

          "Then it should terminates" in {
            val game = TestProbe()
            val airControl = TestProbe()
            val plane = system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")

            val probe = TestProbe()
            probe watch plane

            probe expectTerminated(plane, (2 * Plane.OUT_OF_KEROZEN_TIMEOUT).milliseconds)
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a flying plane" - {

        "When the airControl request the plane to land on the runway" - {

          "Then the plane should land withing timeout" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val runway = TestProbe()
            val plane = system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")
            airControl expectMsg Incoming

            //When
            airControl.send(plane, Command.Land(runway.ref))

            //Then
            airControl expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)
            runway expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a landed plane" - {

        "When the plane is requested to contact ground control" - {

          "Then the plane should make the contact" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val plane = system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")
            airControl expectMsg Incoming
            airControl.send(plane, Command.Land(TestProbe().ref))
            airControl expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)

            //When
            airControl.send(plane, Contact(groundControl.ref))

            //Then
            groundControl expectMsg Incoming
            groundControl.lastSender should be(plane)
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a landed plane" - {

        "When the plane is requested taxi" - {

          "Then it should informs airControl, runway, groundControl and taxiway of its movement" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val runway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")
            airControl expectMsg Incoming
            airControl.send(plane, Command.Land(runway.ref))
            airControl expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)
            runway expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)
            airControl.send(plane, Contact(groundControl.ref))
            groundControl expectMsg Incoming

            //When
            groundControl.send(plane, Command.TaxiAndPark(taxiway.ref, gate.ref))

            //Then
            runway expectMsg HasLeft
            airControl expectMsg HasLeft
            taxiway expectMsg TaxiingToGate(gate.ref)
            groundControl expectMsg StartTaxi
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a taxiing plane" - {

        "When the plane exits from the taxiway" - {

          "Then it should informs the groundcontrol of its movement" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")
            airControl expectMsg Incoming
            airControl.send(plane, Command.Land(TestProbe().ref))
            airControl expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)
            airControl.send(plane, Contact(groundControl.ref))
            groundControl expectMsg Incoming
            groundControl.send(plane, Command.TaxiAndPark(taxiway.ref, gate.ref))
            groundControl expectMsg StartTaxi

            //When
            taxiway.send(plane, HasParked)

            //Then
            groundControl expectMsg HasParked
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a parked plane" - {

        "When the plane has finished unloading passengers" - {

          "Then it should terminates and notify groundControl and gate, and scores the game" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(Props(classOf[Plane], airControl.ref, game.ref), "plane")
            val probe = TestProbe()
            probe watch plane
            airControl expectMsg Incoming
            airControl.send(plane, Command.Land(TestProbe().ref))
            airControl expectMsg(2 * Plane.MAX_LANDING_TIMEOUT.milliseconds, HasLanded)
            airControl.send(plane, Contact(groundControl.ref))
            groundControl expectMsg Incoming
            groundControl.send(plane, Command.TaxiAndPark(taxiway.ref, gate.ref))
            groundControl expectMsg StartTaxi
            taxiway.send(plane, HasParked)
            groundControl expectMsg HasParked

            //Then
            probe expectTerminated(plane, 2 * Plane.MAX_UNLOADING_PASSENGERS_TIMEOUT.milliseconds)
            gate expectMsg HasLeft
            groundControl expectMsg HasLeft
            game expectMsg Score(10)
          }
        }
      }
  }
}
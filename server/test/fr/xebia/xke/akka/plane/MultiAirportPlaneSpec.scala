package fr.xebia.xke.akka.plane

import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent._
import fr.xebia.xke.akka.airport.command._
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers
import akka.event.EventStream
import fr.xebia.xke.akka.ActorSpecs
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.airport.command.Land
import fr.xebia.xke.akka.airport.command.ParkAt
import fr.xebia.xke.akka.airport.command.Taxi
import fr.xebia.xke.akka.airport.command.Contact

class MultiAirportPlaneSpec extends ActorSpecs with ShouldMatchers {

  val settings = Settings.TEST

  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "When it starts" - {

          "Then it should contact the aircontrol" in {
            val game = TestProbe()
            val airControl = TestProbe()

            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "Given the radio fability is 0.5" - {

          "When airControl ask for a command" - {

            "Ask is received half of the time" in {
              val airControl = TestProbe()

              for (_ <- 1 to 10) {
                val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, TestProbe().ref, settings.copy(radioReliability = 0.5, ackMaxDuration = 50), new EventStream()))

                airControl.send(plane, Contact(airControl.ref))
                airControl.send(plane, Land(TestProbe().ref))

              }

              airControl.receiveWhile(1000 milliseconds) {
                case Ack => 1
                case _ => 0
              }.sum should (be < 10 and be > 0)
            }
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
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            val probe = TestProbe()
            probe watch plane

            probe expectTerminated(plane, (2 * settings.outOfKerozenTimeout).milliseconds)
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a flying plane" - {

        "When the airControl request the plane to land on the runway" - {

          "Then the plane should ack and land withing timeout" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val runway = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming

            //When
            airControl reply Land(runway.ref)

            //Then
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            runway expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
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
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))


            airControl expectMsg Incoming
            airControl reply Land(TestProbe().ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)

            //When
            airControl reply Contact(groundControl.ref)

            //Then
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            groundControl.lastSender should be(plane)
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a landed plane" - {

        "When the plane is requested to taxi" - {

          "Then it should informs airControl, runway, groundControl and taxiway of its movement" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val runway = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            airControl reply Land(runway.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            runway expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            airControl reply Contact(groundControl.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming

            //When
            groundControl reply Taxi(taxiway.ref)

            //Then
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            runway expectMsg HasLeft
            airControl expectMsg HasLeft
            taxiway expectMsg Taxiing

          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a taxiing plane" - {

        "When the plane exits from the taxiway" - {

          "Then it should ask the groundcontrol for a gate" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            airControl reply Land(TestProbe().ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            airControl reply Contact(groundControl.ref)
            groundControl expectMsg Incoming
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl reply Taxi(taxiway.ref)
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing

            //When
            taxiway.send(plane, EndOfTaxi)

            //Then
            groundControl expectMsg EndOfTaxi
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a parked plane" - {

        "When the plane is requested to park" - {

          "Then it should leave the taxiway and park at the gate" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            airControl reply Land(TestProbe().ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            airControl reply Contact(groundControl.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            groundControl reply Taxi(taxiway.ref)
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing
            taxiway.send(plane, EndOfTaxi)
            groundControl expectMsg EndOfTaxi

            //When
            groundControl reply ParkAt(gate.ref)

            //Then
            taxiway expectMsg HasLeft
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            gate expectMsg HasParked
            groundControl expectMsg HasParked
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a parked plane" - {

        "When the plane has finished unloading passengers" - {

          "Then it requests takeoff" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            val probe = TestProbe()
            probe watch plane
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            airControl reply Land(TestProbe().ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            airControl reply Contact(groundControl.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            groundControl reply Taxi(taxiway.ref)
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing
            taxiway.send(plane, EndOfTaxi)
            groundControl expectMsg EndOfTaxi
            groundControl reply ParkAt(gate.ref)
            taxiway expectMsg HasLeft
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            gate expectMsg HasParked
            groundControl expectMsg HasParked

            //Then
            groundControl expectMsg RequestTakeoff
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a plane ready to takeoff" - {

        "When is requested to takeoff" - {

          "Then it contact the destination airport" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            val probe = TestProbe()
            probe watch plane

            TestProbe().send(plane, Contact(airControl.ref))
            airControl expectMsg Incoming
            airControl reply Land(TestProbe().ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            airControl reply Contact(groundControl.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            groundControl reply Taxi(taxiway.ref)
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing
            taxiway.send(plane, EndOfTaxi)
            groundControl expectMsg EndOfTaxi
            groundControl reply ParkAt(gate.ref)
            taxiway expectMsg HasLeft
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            gate expectMsg HasParked
            groundControl expectMsg HasParked
            groundControl expectMsg RequestTakeoff

            val destinationAirport = TestProbe()
            groundControl.send(plane, Takeoff(destinationAirport.ref))

            //Then
            destinationAirport expectMsg Incoming
          }
        }
      }
  }

  `Given an actor system` {
    implicit system =>

      "Given a plane has contacted the destination airport before takeoff" - {

        "When the destination airport ask the plane to land" - {

          "Then it should notify gate and groundControl" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(MultiAirportPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            val probe = TestProbe()
            TestProbe().send(plane, Contact(airControl.ref))

            probe watch plane
            airControl expectMsg Incoming
            airControl reply Land(TestProbe().ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            airControl reply Contact(groundControl.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            groundControl reply Taxi(taxiway.ref)
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing
            taxiway.send(plane, EndOfTaxi)
            groundControl expectMsg EndOfTaxi
            groundControl reply ParkAt(gate.ref)
            taxiway expectMsg HasLeft
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            gate expectMsg HasParked
            groundControl expectMsg HasParked
            groundControl expectMsg RequestTakeoff

            val destinationAirport = TestProbe()
            groundControl.send(plane, Takeoff(destinationAirport.ref))
            destinationAirport expectMsg Incoming

            val otherRunway = TestProbe()
            destinationAirport.send(plane, Land(otherRunway.ref))

            //Then
            gate expectMsg HasLeft
            groundControl expectMsg HasLeft
          }
        }
      }
  }
}
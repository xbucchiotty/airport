package fr.xebia.xke.akka.plane

import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.message.PlaneEvent
import PlaneEvent.{EndOfTaxi, HasParked, Taxiing, HasLeft, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.message.command.{ParkAt, Taxi, Contact, Land}
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers
import akka.event.EventStream
import fr.xebia.xke.akka.ActorSpecs
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack

class JustParkingPlaneSpec extends ActorSpecs with ShouldMatchers {

  val settings = Settings.TEST

  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "When it starts" - {

          "Then it should contact the aircontrol" in {
            val airControl = TestProbe()

            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
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
                val plane = system.actorOf(JustParkingPlane.props(settings.copy(radioReliability = 0.5, ackMaxDuration = 50), new EventStream(system)))
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
            val airControl = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
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
            val airControl = TestProbe()
            val runway = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
            val operator = TestProbe()
            TestProbe().send(plane, Contact(airControl.ref))
            airControl expectMsg Incoming

            //When
            operator.send(plane, Land(runway.ref))

            //Then
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
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
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
            val operator = TestProbe()

            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            operator.send(plane, Land(TestProbe().ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)

            //When
            operator reply Contact(groundControl.ref)

            //Then
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
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
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val runway = TestProbe()
            val operator = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            operator.send(plane, Land(runway.ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            runway expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            operator reply Contact(groundControl.ref)
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming

            //When
            operator reply Taxi(taxiway.ref)

            //Then
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
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
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
            val operator = TestProbe()
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            operator.send(plane, Land(TestProbe().ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            operator reply Contact(groundControl.ref)
            groundControl expectMsg Incoming
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            operator reply Taxi(taxiway.ref)
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

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
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val operator = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
            operator.send(plane, Land(TestProbe().ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            operator reply Contact(groundControl.ref)
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            operator.send(plane, Taxi(taxiway.ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing
            taxiway.send(plane, EndOfTaxi)
            groundControl expectMsg EndOfTaxi

            //When
            operator reply ParkAt(gate.ref)

            //Then
            taxiway expectMsg HasLeft
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
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

          "Then it should terminates and notify groundControl and gate" in {
            //Given
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val gate = TestProbe()
            val plane = system.actorOf(JustParkingPlane.props(settings, new EventStream(system)), "plane")
            val probe = TestProbe()
            val operator = TestProbe()
            TestProbe().send(plane, Contact(airControl.ref))

            probe watch plane
            airControl expectMsg Incoming
            operator.send(plane, Land(TestProbe().ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            operator reply Contact(groundControl.ref)
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl expectMsg Incoming
            operator.send(plane, Taxi(taxiway.ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing
            taxiway.send(plane, EndOfTaxi)
            groundControl expectMsg EndOfTaxi
            operator reply ParkAt(gate.ref)
            taxiway expectMsg HasLeft
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            gate expectMsg HasParked
            groundControl expectMsg HasParked

            //Then
            probe expectTerminated(plane, 2 * settings.unloadingPassengersMaxDuration.milliseconds)
            groundControl expectMsg HasLeft
            gate expectMsg HasLeft
          }
        }
      }
  }
}
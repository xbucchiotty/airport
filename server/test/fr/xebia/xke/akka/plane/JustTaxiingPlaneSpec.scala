package fr.xebia.xke.akka.plane

import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent.{HasParked, EndOfTaxi, Taxiing, HasLeft, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.command.{Taxi, Ack, Land, Contact}
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers
import akka.event.EventStream
import fr.xebia.xke.akka.ActorSpecs
import fr.xebia.xke.akka.game.Settings

class JustTaxiingPlaneSpec extends ActorSpecs with ShouldMatchers {

  val settings = Settings.TEST

  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "When it starts" - {

          "Then it should contact the aircontrol" in {
            val airControl = TestProbe()

            val plane = system.actorOf(JustTaxiingPlane.props(settings, new EventStream()), "plane")
            TestProbe().send(plane, Contact(airControl.ref))

            airControl expectMsg Incoming
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
            val plane = system.actorOf(JustTaxiingPlane.props(settings, new EventStream()), "plane")
            val operator = TestProbe()
            operator.send(plane, Contact(airControl.ref))

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
            val plane = system.actorOf(JustTaxiingPlane.props(settings, new EventStream()), "plane")
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
            val plane = system.actorOf(JustTaxiingPlane.props(settings, new EventStream()), "plane")
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
            operator.send(plane, Taxi(taxiway.ref))

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

          "Then it should terminates" in {
            //Given
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val plane = system.actorOf(JustTaxiingPlane.props(settings, new EventStream()), "plane")
            val probe = TestProbe()
            val operator = TestProbe()
            TestProbe().send(plane, Contact(airControl.ref))

            probe watch plane
            airControl expectMsg Incoming
            operator.send(plane, Land(TestProbe().ref))
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            operator reply Contact(groundControl.ref)
            groundControl expectMsg Incoming
            operator expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            groundControl reply Taxi(taxiway.ref)
            groundControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)

            taxiway expectMsg Taxiing

            //When
            taxiway.send(plane, EndOfTaxi)

            //Then
            groundControl expectMsg HasParked
            taxiway expectMsg HasLeft
            probe expectTerminated plane
          }
        }
      }
  }
}
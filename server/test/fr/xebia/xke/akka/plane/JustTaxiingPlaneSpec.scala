package fr.xebia.xke.akka.plane

import akka.actor.Props
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
            val game = TestProbe()
            val airControl = TestProbe()

            system.actorOf(JustTaxiingPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")

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
            val game = TestProbe()
            val airControl = TestProbe()
            val runway = TestProbe()
            system.actorOf(JustTaxiingPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
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
            val plane = system.actorOf(JustTaxiingPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")

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
            val gate = TestProbe()
            system.actorOf(JustTaxiingPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
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

          "Then it should terminates" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val taxiway = TestProbe()
            val plane = system.actorOf(JustTaxiingPlane.props(airControl.ref, game.ref, settings, new EventStream()), "plane")
            val probe = TestProbe()
            probe watch plane
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
            groundControl expectMsg HasParked
            taxiway expectMsg HasLeft
            probe expectTerminated plane
          }
        }
      }
  }
}
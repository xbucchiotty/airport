package fr.xebia.xke.akka.plane

import akka.actor.Props
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, HasLanded, Incoming}
import languageFeature.postfixOps
import org.scalatest.ShouldMatchers
import fr.xebia.xke.akka.airport.command.{Contact, Land, Ack}
import akka.event.EventStream
import fr.xebia.xke.akka.ActorSpecs
import fr.xebia.xke.akka.game.Settings

class JustLandingPlaneSpec extends ActorSpecs with ShouldMatchers {

  val settings = Settings.TEST

  `Given an actor system` {
    implicit system =>

      "Given a plane" - {

        "When it starts" - {

          "Then it should contact the aircontrol" in {
            val game = TestProbe()
            val airControl = TestProbe()

            system.actorOf(Props(classOf[JustLandingPlane], airControl.ref, game.ref, settings, new EventStream()), "plane")

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
            val plane = system.actorOf(Props(classOf[JustLandingPlane], airControl.ref, game.ref, settings, new EventStream()), "plane")

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
            system.actorOf(Props(classOf[JustLandingPlane], airControl.ref, game.ref, settings, new EventStream()), "plane")
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

          "Then it should leaves runway and air traffic control" in {
            //Given
            val game = TestProbe()
            val airControl = TestProbe()
            val groundControl = TestProbe()
            val runway = TestProbe()
            system.actorOf(Props(classOf[JustLandingPlane], airControl.ref, game.ref, settings, new EventStream()), "plane")

            airControl expectMsg Incoming
            airControl reply Land(runway.ref)
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            airControl expectMsg(2 * settings.landingMaxDuration.milliseconds, HasLanded)
            runway expectMsg HasLanded

            //When
            airControl reply Contact(groundControl.ref)

            //Then
            airControl expectMsg(2 * settings.ackMaxDuration.milliseconds, Ack)
            runway expectMsg HasLeft
            airControl expectMsg HasLeft
          }
        }
      }
  }

}
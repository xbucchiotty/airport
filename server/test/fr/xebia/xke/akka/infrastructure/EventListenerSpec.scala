package fr.xebia.xke.akka.infrastructure

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.{Address, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Second, Millis, Span}
import concurrent.duration._
import language.postfixOps
import controllers.DequeueEvents
import fr.xebia.xke.akka.game._
import fr.xebia.xke.akka.game.PlayerDown
import fr.xebia.xke.akka.game.Score
import fr.xebia.xke.akka.game.PlayerUp
import scala.Some
import fr.xebia.xke.akka.plane.event.PlaneStatus

class EventListenerSpec extends FunSpec with ShouldMatchers with Eventually {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(1, Second), interval = Span(100, Millis))

  describe("Event listener") {

    it("should enqueue events") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      system.eventStream.publish(Score(10, 20))
      system.eventStream.publish(Score(20, 20))

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "Score" ,
      "current": "10",
      "objective": "20"
   }""".stripMargin))
      }

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "Score" ,
      "current": "20",
      "objective": "20"
   }""".stripMargin))
      }
    }
    it("should dequeue a score event") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      system.eventStream.publish(Score(10, 20))


      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "Score" ,
      "current": "10",
      "objective": "20"
   }""".stripMargin))
      }
    }

    it("should dequeue a playerUp event") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      val address = Address("tcp", "localhost")
      system.eventStream.publish(PlayerUp(SessionId("xbucchiotty@xebia.fr"), address))

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "PlayerUp",
      "address" : "tcp://localhost"
    }""".stripMargin))
      }
    }

    it("should dequeue a playerDown event") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      val address = Address("tcp", "localhost")
      system.eventStream.publish(PlayerDown(SessionId("xbucchiotty@xebia.fr"), address))

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "PlayerDown",
      "address" : "tcp://localhost"
    }""".stripMargin))
      }
    }

    it("should dequeue a planeStatus event") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      system.eventStream.publish(PlaneStatus("Runway", "AF-000", "test", "no-error"))

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
   "type" : "PlaneStatus" ,
   "step" : "runway" ,
   "flightName" : "AF-000" ,
   "detail" : "test" ,
   "error" : "no-error"
   }""".stripMargin))
      }
    }

    it("should dequeue a GameOver event and unsubscribe") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      system.eventStream.publish(GameOver)

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "GameOver"
   }""".stripMargin))

        system.eventStream.publish(PlaneStatus("Runway", "AF-000", "test", "no-error"))

        eventually {
          probe.send(listener, DequeueEvents)
          probe.expectMsg(None)
        }
      }
    }

    it("should dequeue a GameEnd event and unsubscribe") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val listener = system.actorOf(EventListener.props(system.eventStream))

      val probe = TestProbe()

      system.eventStream.publish(GameEnd)

      eventually {
        probe.send(listener, DequeueEvents)
        probe.receiveOne(100 milliseconds) should equal(Some( """{
      "type" : "GameEnd"
   }""".stripMargin))

        system.eventStream.publish(PlaneStatus("Runway", "AF-000", "test", "no-error"))

        eventually {
          probe.send(listener, DequeueEvents)
          probe.expectMsg(None)
        }
      }
    }
  }
}

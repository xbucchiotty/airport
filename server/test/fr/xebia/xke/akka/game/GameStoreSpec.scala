package fr.xebia.xke.akka.game

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.{Address, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import GameStore.GameStarted
import akka.testkit.TestProbe
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Second, Span}
import akka.pattern.ask
import fr.xebia.xke.akka.infrastructure.{SessionId, SessionInfo}
import fr.xebia.xke.akka.game.GameStore.GameCreated
import scala.Some
import fr.xebia.xke.akka.game.GameStore.NewGame
import fr.xebia.xke.akka.game.GameStore.StartGame
import fr.xebia.xke.akka.plane.JustParkingPlane
import fr.xebia.xke.akka.airport.{AirportCode, Airport}
import java.util.UUID

class GameStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(1, Second), interval = Span(100, Millis))


  describe("A game store") {

    it("should create a game for a session") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")
      val userInfo = SessionInfo(SessionId(new UUID(0, 0)), Airport("Paris", AirportCode("CDG"), "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[JustParkingPlane]))

      probe expectMsgAllClassOf classOf[GameCreated]
    }

    it("should be able to start a created game") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val sessionId = SessionId(new UUID(0, 0))
      val userInfo = SessionInfo(sessionId, Airport("Paris", AirportCode("CDG"), "42", "2"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")

      val probe = TestProbe()
      val airTrafficControl = TestProbe()
      val groundControl = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[JustParkingPlane]))
      probe expectMsgAllClassOf classOf[GameCreated]

      probe.send(gameStore, StartGame(userInfo, airTrafficControl.ref, groundControl.ref))
      probe expectMsg GameStarted
    }

    it("should returns the context of an existing sessionId") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")
      val sessionId = SessionId(new UUID(0, 0))
      val userInfo = SessionInfo(sessionId, Airport("Paris", AirportCode("CDG"), "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[JustParkingPlane]))

      probe expectMsgAllClassOf classOf[GameCreated]

      whenReady(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]]) {
        reply =>
          reply should be(defined)
      }
    }

    it("should not returns the context of an unknown sessionId") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")
      val sessionId = SessionId(new UUID(0, 0))

      whenReady(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]]) {
        reply => reply should not(be(defined))
      }
    }

    it("should publish into the stream PlayerUp event if session is known") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")
      val sessionId = SessionId(new UUID(0, 0))
      val userInfo = SessionInfo(sessionId, Airport("Paris", AirportCode("CDG"), "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[JustParkingPlane]))
      probe expectMsgAllClassOf classOf[GameCreated]

      whenReady(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]]) {
        case Some(reply) =>
          val listener = TestProbe()
          reply.eventBus.subscribe(listener.ref, classOf[GameEvent])

          val event = PlayerUp(sessionId, Address("tcp", "TestSystem"))
          probe.send(gameStore, event)

          listener.expectMsg(event)
      }
    }

    it("should publish into the stream PlayerDown event if session is known") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")
      val sessionId = SessionId(new UUID(0, 0))
      val userInfo = SessionInfo(sessionId, Airport("Paris", AirportCode("CDG"), "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[JustParkingPlane]))
      probe expectMsgAllClassOf classOf[GameCreated]

      whenReady(ask(gameStore, GameStore.Ask(sessionId)).mapTo[Option[GameContext]]) {
        case Some(reply) =>
          val listener = TestProbe()
          reply.eventBus.subscribe(listener.ref, classOf[GameEvent])

          val event = PlayerDown(sessionId, Address("tcp", "TestSystem"))
          probe.send(gameStore, event)

          listener.expectMsg(event)
      }
    }
  }
}
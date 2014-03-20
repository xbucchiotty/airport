package fr.xebia.xke.akka.airport.game

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.{Address, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import GameStore.{GameStarted, GameCreated}
import fr.xebia.xke.akka.airport._
import akka.testkit.TestProbe
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Second, Span}
import akka.pattern.ask
import fr.xebia.xke.akka.airport.PlayerDown
import fr.xebia.xke.akka.airport.game.GameStore.NewGame
import fr.xebia.xke.akka.airport.PlayerUp
import fr.xebia.xke.akka.airport.game.GameStore.StartGame
import scala.Some
import fr.xebia.xke.akka.airport.plane.FullStepPlane

class GameStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(1, Second), interval = Span(100, Millis))


  describe("A game store") {

    it("should create a game for a user") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(null), "gameStore")
      val userInfo = UserInfo(TeamMail("xbucchiotty@xebia.fr"), Airport("Paris", "CDG", "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))

      probe expectMsg GameCreated
    }

    it("should be able to start a created game") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val userId = TeamMail("xbucchiotty@xebia.fr")
      val userInfo = UserInfo(userId, Airport("Paris", "CDG", "42", "2"), Some(Address("tcp", "TestSystem")))
      val gameStore = system.actorOf(GameStore.props(null), "gameStore")

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))
      probe expectMsg GameCreated

      probe.send(gameStore, StartGame(userInfo))
      probe expectMsg GameStarted
    }

    it("should returns the context of an existing userId") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(null), "gameStore")
      val userId = TeamMail("xbucchiotty@xebia.fr")
      val userInfo = UserInfo(userId, Airport("Paris", "CDG", "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))

      probe expectMsg GameCreated

      whenReady(ask(gameStore, GameStore.Ask(userId)).mapTo[Option[GameContext]]) {
        reply =>
          reply should be(defined)
      }
    }

    it("should not returns the context of an unknown userId") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(null), "gameStore")
      val userId = TeamMail("xbucchiotty@xebia.fr")

      whenReady(ask(gameStore, GameStore.Ask(userId)).mapTo[Option[GameContext]]) {
        reply => reply should not(be(defined))
      }
    }

    it("should publish into the stream PlayerUp event if user is known") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(null), "gameStore")
      val userId = TeamMail("xbucchiotty@xebia.fr")
      val userInfo = UserInfo(userId, Airport("Paris", "CDG", "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))
      probe expectMsg GameCreated

      whenReady(ask(gameStore, GameStore.Ask(userId)).mapTo[Option[GameContext]]) {
        case Some(reply) =>
          val listener = TestProbe()
          reply.eventBus.subscribe(listener.ref, classOf[GameEvent])

          val event = PlayerUp(userId, Address("tcp", "TestSystem"))
          probe.send(gameStore, event)

          listener.expectMsg(event)
      }
    }

    it("should publish into the stream PlayerDown event if user is known") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(null), "gameStore")
      val userId = TeamMail("xbucchiotty@xebia.fr")
      val userInfo = UserInfo(userId, Airport("Paris", "CDG", "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))
      probe expectMsg GameCreated

      whenReady(ask(gameStore, GameStore.Ask(userId)).mapTo[Option[GameContext]]) {
        case Some(reply) =>
          val listener = TestProbe()
          reply.eventBus.subscribe(listener.ref, classOf[GameEvent])

          val event = PlayerDown(userId, Address("tcp", "TestSystem"))
          probe.send(gameStore, event)

          listener.expectMsg(event)
      }
    }
  }
}
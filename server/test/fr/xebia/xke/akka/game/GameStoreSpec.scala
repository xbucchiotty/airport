package fr.xebia.xke.akka.game

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import GameStore.GameStarted
import akka.testkit.TestProbe
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Second, Span}
import akka.pattern.ask
import fr.xebia.xke.akka.infrastructure.SessionId
import fr.xebia.xke.akka.game.GameStore.GameCreated
import fr.xebia.xke.akka.game.GameStore.NewGame
import fr.xebia.xke.akka.game.GameStore.StartGame
import fr.xebia.xke.akka.plane.JustParkingPlane
import fr.xebia.xke.akka.airport.Airport
import akka.event.EventStream
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator

class GameStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(1, Second), interval = Span(100, Millis))


  describe("A game store") {

    it("should create a game for an airport") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportLocator = TestProbe()
      val gameStore = system.actorOf(GameStore.props(airportLocator.ref, new EventStream()), "gameStore")
      val airport = Airport.top100.head

      val probe = TestProbe()
      probe.send(gameStore, NewGame(airport, Settings.TEST, classOf[JustParkingPlane]))

      airportLocator.expectMsg(AirportLocator.AskAddress(airport.code))
      airportLocator.reply(gameStore.path.address)

      val gameCreated = probe.receiveOne(100 milliseconds).asInstanceOf[GameCreated]
      gameCreated.gameContext.airport should equal(airport)
      gameCreated.gameContext.settings should equal(Settings.TEST)
    }

    it("should be able to start a created game") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportLocator = TestProbe()
      val gameStore = system.actorOf(GameStore.props(airportLocator.ref, new EventStream()), "gameStore")
      val airport = Airport.top100.head
      val probe = TestProbe()
      probe.send(gameStore, NewGame(airport, Settings.TEST, classOf[JustParkingPlane]))
      airportLocator.expectMsg(AirportLocator.AskAddress(airport.code))
      airportLocator.reply(gameStore.path.address)
      val gameContext = probe.receiveOne(100 milliseconds).asInstanceOf[GameCreated].gameContext

      probe.send(gameStore, GameStore.StartGame(gameContext.sessionId))
      airportLocator.expectMsg(AirportLocator.CreateClient(airport.code, gameContext.sessionId))
      airportLocator.reply(TestProbe().ref)

      probe expectMsg GameStarted
    }

    it("should returns the context of an existing sessionId") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportLocator = TestProbe()
      val gameStore = system.actorOf(GameStore.props(airportLocator.ref, new EventStream()), "gameStore")
      val airport = Airport.top100.head
      val probe = TestProbe()
      probe.send(gameStore, NewGame(airport, Settings.TEST, classOf[JustParkingPlane]))
      val gameContext = probe.receiveOne(100 milliseconds).asInstanceOf[GameCreated].gameContext
      airportLocator.expectMsg(AirportLocator.AskAddress(airport.code))
      airportLocator.reply(gameStore.path.address)
      probe.send(gameStore, StartGame(gameContext.sessionId))
      airportLocator.expectMsg(AirportLocator.CreateClient(airport.code, gameContext.sessionId))
      airportLocator.reply(TestProbe().ref)
      probe expectMsg GameStarted

      whenReady(ask(gameStore, GameStore.Ask(gameContext.sessionId)).mapTo[Option[GameContext]]) {
        reply => reply should be(defined)
      }
    }

    it("should not returns the context of an unknown sessionId") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(TestProbe().ref, new EventStream()), "gameStore")

      whenReady(ask(gameStore, GameStore.Ask(SessionId())).mapTo[Option[GameContext]]) {
        reply => reply should not(be(defined))
      }
    }

    it("should publish into the stream PlayerUp event if session is known") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val clusterEventStream = new EventStream()
      val gameStore = system.actorOf(GameStore.props(TestProbe().ref, clusterEventStream), "gameStore")
      val airport = Airport.top100.head
      val probe = TestProbe()
      probe.send(gameStore, NewGame(airport, Settings.TEST, classOf[JustParkingPlane]))
      val gameContext = probe.receiveOne(100 milliseconds).asInstanceOf[GameCreated].gameContext
      val gameStreamProbe = TestProbe()
      gameContext.eventBus.subscribe(gameStreamProbe.ref, classOf[Any])

      clusterEventStream.publish(AirportLocator.AirportConnected(airport.code, gameStore.path.address))

      gameStreamProbe expectMsg PlayerUp(gameStore.path.address)

    }

    it("should publish into the stream PlayerDown event if session is known") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val clusterEventStream = new EventStream()
      val gameStore = system.actorOf(GameStore.props(TestProbe().ref, clusterEventStream), "gameStore")
      val airport = Airport.top100.head
      val probe = TestProbe()
      probe.send(gameStore, NewGame(airport, Settings.TEST, classOf[JustParkingPlane]))
      val gameContext = probe.receiveOne(100 milliseconds).asInstanceOf[GameCreated].gameContext
      val gameStreamProbe = TestProbe()
      gameContext.eventBus.subscribe(gameStreamProbe.ref, classOf[Any])

      clusterEventStream.publish(AirportLocator.AirportDisconnected(airport.code, gameStore.path.address))

      gameStreamProbe expectMsg PlayerDown(gameStore.path.address)
    }
  }
}
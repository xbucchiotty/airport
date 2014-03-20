package fr.xebia.xke.akka.airport.game

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.{Address, ActorSystem}
import akka.pattern.ask
import PlayerStore._
import fr.xebia.xke.akka.airport.{PlayerUp, Airport}
import org.scalatest.concurrent.ScalaFutures
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import org.scalatest.time.{Seconds, Millis, Span}
import com.typesafe.config.ConfigFactory
import PlayerStore.BindActorSystem
import PlayerStore.BoundActorSystem
import akka.testkit.TestProbe

class PlayerStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  implicit val timeout = Timeout(5 seconds)

  implicit val airports = Airport.top100

  describe("A player store store") {

    it("should be able to bind an actor system address to an airport") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()
      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val actorSystemAddress = Address("tcp", "testSystem", "localhost", 9000)

      val registration = ask(playerStore, Register(TeamMail("xbucchiotty@xebia.fr"))).mapTo[Registered]

      whenReady(registration) {
        registrationInfo =>
          val roles = Set(registrationInfo.userInfo.airportCode)

          whenReady(ask(playerStore, BindActorSystem(actorSystemAddress, roles)).mapTo[BoundActorSystem]) {
            boundMessage =>
              boundMessage.address should equal(actorSystemAddress)
              boundMessage.airport.code should equal(registrationInfo.userInfo.airportCode)
          }
      }

      gameStore expectMsg PlayerUp(TeamMail("xbucchiotty@xebia.fr"), actorSystemAddress)
    }

    it("should not allow to bind an airport to two different addresses") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()
      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val firstAddress = Address("tcp", "testSystem", "localhost", 9000)
      val secondAddress = Address("tcp", "testSystem", "localhost", 9001)
      val registration = ask(playerStore, Register(TeamMail("xbucchiotty@xebia.fr"))).mapTo[Registered]

      whenReady(registration) {
        registrationInfo =>
          val roles = Set(registrationInfo.userInfo.airportCode)

          whenReady(ask(playerStore, BindActorSystem(firstAddress, roles)).mapTo[BoundActorSystem]) {
            _ =>
              whenReady(ask(playerStore, BindActorSystem(secondAddress, roles)).mapTo[BindError]) {
                error =>
                  error.message should equal(s"Airport ${registrationInfo.userInfo.airportCode} is already bound to a system")
              }
          }
      }
    }

    it("should not allow bind a system without any user registered") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val playerStore = system.actorOf(PlayerStore.props(null, null), "playerStore")

      val address = Address("tcp", "testSystem", "localhost", 9000)

      whenReady(ask(playerStore, BindActorSystem(address, Set("JFK"))).mapTo[BindError]) {
        bindError => bindError.message should equal("Impossible to bind an airport in Set(JFK) without any user registered before")
      }
    }

    it("should be idempotent for actor system binding") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()

      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val address = Address("tcp", "testSystem", "localhost", 9000)

      val registration = ask(playerStore, Register(TeamMail("xbucchiotty@xebia.fr"))).mapTo[Registered]

      whenReady(registration) {
        registrationInfo =>

          whenReady(ask(playerStore, BindActorSystem(address, Set(registrationInfo.userInfo.airportCode))).mapTo[BoundActorSystem]) {
            firstReply =>

              whenReady(ask(playerStore, BindActorSystem(address, Set("CDG"))).mapTo[BoundActorSystem]) {

                secondReply =>
                  secondReply should equal(firstReply)
              }
          }
      }
    }

    it("should unbind an existing actor system binding") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()

      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val address = Address("tcp", "testSystem", "localhost", 9000)

      val registration = ask(playerStore, Register(TeamMail("xbucchiotty@xebia.fr"))).mapTo[Registered]

      whenReady(registration) {
        registrationInfo =>

          whenReady(ask(playerStore, BindActorSystem(address, Set(registrationInfo.userInfo.airportCode))).mapTo[BoundActorSystem]) {
            _ =>

              whenReady(ask(playerStore, UnbindActorSystem(address, Set("CDG"))).mapTo[UnboundActorSystem]) {

                reply =>
                  reply.address should equal(address)
                  reply.airport.code should equal(registrationInfo.userInfo.airportCode)
              }
          }
      }
    }
    it("should be able to register a new user") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val playerStore = system.actorOf(PlayerStore.props(null, null), "playerStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      whenReady(ask(playerStore, Register(userId)).mapTo[Registered]) {
        boundMessage =>
          boundMessage.userInfo.userId should equal(userId)
          airports should contain(boundMessage.userInfo.airport)
      }
    }

    it("should not allow to register two user with the same email") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val playerStore = system.actorOf(PlayerStore.props(null, null), "playerStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      whenReady(ask(playerStore, Register(userId)).mapTo[Registered]) {
        firstReply =>

          whenReady(ask(playerStore, Register(userId)).mapTo[RegisterError]) {
            error =>
              error.message should equal("Email already registered")

          }
      }
    }

    it("should give the airport of a bound user") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val playerStore = system.actorOf(PlayerStore.props(null, null), "playerStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      whenReady(ask(playerStore, Register(userId)).mapTo[Registered]) {
        firstReply =>

          whenReady(ask(playerStore, Ask(userId)).mapTo[Option[UserInfo]]) {
            userInfo =>
              userInfo should be(defined)
              userInfo.foreach(info => {
                info.airport should equal(firstReply.userInfo.airport)
                info.playerSystemAddress should not(be(defined))
              })
          }
      }
    }

    it("should give the address of the player when system is bound") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()
      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      val probe = TestProbe()

      probe.send(playerStore, Register(userId))
      val registration = probe.receiveOne(timeout.duration).asInstanceOf[Registered]

      val address = Address("tcp", "TestSystem", "localhost", 9000)
      probe.send(playerStore, BindActorSystem(address, Set(registration.userInfo.airportCode)))
      probe.receiveN(1)

      probe.send(playerStore, Ask(userId))
      val reply = probe.receiveOne(timeout.duration).asInstanceOf[Option[UserInfo]]

      reply should be(defined)
      reply.foreach(userInfo => {

        userInfo.playerSystemAddress should be(defined)
        userInfo.playerSystemAddress.foreach(playerAddress => playerAddress should equal(address))
      })
    }

    it("should not give the address of the player when system is not bound yet") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()
      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      val probe = TestProbe()

      probe.send(playerStore, Register(userId))
      probe.receiveOne(timeout.duration)

      probe.send(playerStore, Ask(userId))
      val reply = probe.receiveOne(timeout.duration).asInstanceOf[Option[UserInfo]]

      reply should be(defined)
      reply.foreach(userInfo => {
        userInfo.playerSystemAddress should not(be(defined))
      })
    }

    it("should not give the address of the player when system is not unbound") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = TestProbe()
      val playerStore = system.actorOf(PlayerStore.props(gameStore.ref, TestProbe().ref), "playerStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      val probe = TestProbe()

      probe.send(playerStore, Register(userId))
      val registration = probe.receiveOne(timeout.duration).asInstanceOf[Registered]

      val address = Address("tcp", "TestSystem", "localhost", 9000)
      probe.send(playerStore, BindActorSystem(address, Set(registration.userInfo.airportCode)))
      probe.receiveN(1)

      probe.send(playerStore, UnbindActorSystem(address, Set(registration.userInfo.airportCode)))
      probe.receiveN(1)

      probe.send(playerStore, Ask(userId))
      val reply = probe.receiveOne(timeout.duration).asInstanceOf[Option[UserInfo]]

      reply should be(defined)
      reply.foreach(userInfo => {

        userInfo.playerSystemAddress should not(be(defined))
      })
    }

    it("should not give airport of an unknown user") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val playerStore = system.actorOf(PlayerStore.props(null, null), "playerStore")

      val answer = ask(playerStore, Ask(TeamMail("xbucchiotty@xebia.fr"))).mapTo[Option[Airport]]

      whenReady(answer) {
        storedAirport =>
          storedAirport should not(be(defined))
      }
    }
  }
}



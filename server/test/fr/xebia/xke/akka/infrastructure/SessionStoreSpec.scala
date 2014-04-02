package fr.xebia.xke.akka.infrastructure

import org.scalatest._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import akka.util.Timeout
import language.postfixOps
import scala.concurrent.duration._
import akka.pattern.ask
import SessionStore._
import fr.xebia.xke.akka.airport.{AirportCode, Airport}
import java.util.UUID

class SessionStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  implicit val timeout = Timeout(5 seconds)

  val CDGAirport = Airport(
    city = "Paris",
    code = AirportCode("CDG"),
    latitude = "49",
    longitude = "2",
    departures = List.empty,
    arrivals = List.empty)

  val JFKAirport = Airport(
    city = "New York",
    code = AirportCode("JFK"),
    latitude = "40",
    longitude = "-73",
    departures = List.empty,
    arrivals = List.empty)

  val airports = List(CDGAirport, JFKAirport)

  describe("A session store") {

    it("should be able to register a new session") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

      val sessionId = SessionId(new UUID(0, 0))

      whenReady(ask(sessionStore, Register(sessionId)).mapTo[Registered]) {
        boundMessage =>
          boundMessage.userInfo.sessionId should equal(sessionId)
          boundMessage.userInfo.airport should equal(CDGAirport)
      }
    }

    it("should assign airports in order") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

      val sessionId = SessionId(new UUID(0, 0))
      val sessionId2 = SessionId(new UUID(0, 1))

      whenReady(ask(sessionStore, Register(sessionId)).mapTo[Registered]) {
        firstReply =>

          firstReply.userInfo.airport should equal(CDGAirport)

          whenReady(ask(sessionStore, Register(sessionId2)).mapTo[Registered]) {
            secondReply =>

              secondReply.userInfo.airport should equal(JFKAirport)
          }
      }
    }
  }

  it("should not allow to register two sessions with the same email") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

    val sessionId = SessionId(new UUID(0, 0))

    whenReady(ask(sessionStore, Register(sessionId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(sessionStore, Register(sessionId)).mapTo[RegisterError]) {
          error =>
            error.message should equal("SessionId already registered")

        }
    }
  }

  it("should give the info of a registered session") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

    val sessionId = SessionId(new UUID(0, 0))

    whenReady(ask(sessionStore, Register(sessionId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(sessionStore, Ask(sessionId)).mapTo[Option[UserInfo]]) {
          userInfo =>
            userInfo should be(defined)
            userInfo.get.airport should equal(firstReply.userInfo.airport)
        }
    }
  }


  it("should give the session for a registered airport") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

    val sessionId = SessionId(new UUID(0, 0))

    whenReady(ask(sessionStore, Register(sessionId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(sessionStore, AskForAirport(firstReply.userInfo.airportCode)).mapTo[Option[UserInfo]]) {
          userInfo =>
            userInfo should be(defined)
            userInfo.foreach(info => {
              info.airport should equal(firstReply.userInfo.airport)
              info.sessionId should equal(firstReply.userInfo.sessionId)
            })
        }
    }
  }

  it("should not give the session for a unregistered airport") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

    whenReady(ask(sessionStore, AskForAirport(AirportCode("LHR"))).mapTo[Option[UserInfo]]) {
      userInfo =>
        userInfo should not(be(defined))
    }
  }

  it("should not give airport of an unregistered session") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val sessionStore = system.actorOf(SessionStore.props(airports), "sessionStore")

    val answer = ask(sessionStore, Ask(SessionId(new UUID(0, 0)))).mapTo[Option[Airport]]

    whenReady(answer) {
      storedAirport =>
        storedAirport should not(be(defined))
    }
  }

}
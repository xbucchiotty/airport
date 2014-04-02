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
import UserStore._
import fr.xebia.xke.akka.airport.{Route, AirportCode, Airport}

class UserStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

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

  describe("A user store") {

    it("should be able to register a new user") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val userStore = system.actorOf(UserStore.props(airports), "userStore")

      val userId = SessionId("xbucchiotty@xebia.fr")

      whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
        boundMessage =>
          boundMessage.userInfo.userId should equal(userId)
          boundMessage.userInfo.airport should equal(CDGAirport)
      }
    }

    it("should assign airports in order") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val userStore = system.actorOf(UserStore.props(airports), "userStore")

      val userId = SessionId("xbucchiotty@xebia.fr")
      val userId2 = SessionId("info@xebia.fr")

      whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
        firstReply =>

          firstReply.userInfo.airport should equal(CDGAirport)

          whenReady(ask(userStore, Register(userId2)).mapTo[Registered]) {
            secondReply =>

              secondReply.userInfo.airport should equal(JFKAirport)
          }
      }
    }
  }

  it("should not allow to register two users with the same email") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(airports), "userStore")

    val userId = SessionId("xbucchiotty@xebia.fr")

    whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(userStore, Register(userId)).mapTo[RegisterError]) {
          error =>
            error.message should equal("Email already registered")

        }
    }
  }

  it("should give the info of a registered user") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(airports), "userStore")

    val userId = SessionId("xbucchiotty@xebia.fr")

    whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(userStore, Ask(userId)).mapTo[Option[UserInfo]]) {
          userInfo =>
            userInfo should be(defined)
            userInfo.get.airport should equal(firstReply.userInfo.airport)
        }
    }
  }


  it("should give the user for a registered airport") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(airports), "userStore")

    val userId = SessionId("xbucchiotty@xebia.fr")

    whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(userStore, AskForAirport(firstReply.userInfo.airportCode)).mapTo[Option[UserInfo]]) {
          userInfo =>
            userInfo should be(defined)
            userInfo.foreach(info => {
              info.airport should equal(firstReply.userInfo.airport)
              info.userId should equal(firstReply.userInfo.userId)
            })
        }
    }
  }

  it("should not give the user for a unregistered airport") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(airports), "userStore")

    whenReady(ask(userStore, AskForAirport(AirportCode("LHR"))).mapTo[Option[UserInfo]]) {
      userInfo =>
        userInfo should not(be(defined))
    }
  }

  it("should not give airport of an unregistered user") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(airports), "userStore")

    val answer = ask(userStore, Ask(SessionId("xbucchiotty@xebia.fr"))).mapTo[Option[Airport]]

    whenReady(answer) {
      storedAirport =>
        storedAirport should not(be(defined))
    }
  }

}
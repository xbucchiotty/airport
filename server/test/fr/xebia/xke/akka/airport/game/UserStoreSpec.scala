package fr.xebia.xke.akka.airport.game

import org.scalatest._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import akka.util.Timeout
import language.postfixOps
import scala.concurrent.duration._
import akka.pattern.ask
import fr.xebia.xke.akka.airport.Airport
import fr.xebia.xke.akka.airport.game.UserStore._

class UserStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  implicit val timeout = Timeout(5 seconds)

  implicit val airports = Airport.top100

  describe("A user store") {

    it("should be able to register a new user") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val userStore = system.actorOf(UserStore.props(), "userStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")

      whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
        boundMessage =>
          boundMessage.userInfo.userId should equal(userId)
          airports should contain(boundMessage.userInfo.airport)
      }
    }

    it("should assign airports in order of the top100 list") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val userStore = system.actorOf(UserStore.props(), "userStore")

      val userId = TeamMail("xbucchiotty@xebia.fr")
      val userId2 = TeamMail("info@xebia.fr")

      whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
        firstReply =>

          firstReply.userInfo.airport should equal(airports.head)

          whenReady(ask(userStore, Register(userId2)).mapTo[Registered]) {
            secondReply =>

              secondReply.userInfo.airport should equal(airports.drop(1).head)
          }
      }
    }
  }

  it("should not allow to register two users with the same email") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(), "userStore")

    val userId = TeamMail("xbucchiotty@xebia.fr")

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
    val userStore = system.actorOf(UserStore.props(), "userStore")

    val userId = TeamMail("xbucchiotty@xebia.fr")

    whenReady(ask(userStore, Register(userId)).mapTo[Registered]) {
      firstReply =>

        whenReady(ask(userStore, Ask(userId)).mapTo[Option[UserInfo]]) {
          userInfo =>
            userInfo should be(defined)
            userInfo.foreach(info => {
              info.airport should equal(firstReply.userInfo.airport)
            })
        }
    }
  }


  it("should give the user for a registered airport") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(), "userStore")

    val userId = TeamMail("xbucchiotty@xebia.fr")

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
    val userStore = system.actorOf(UserStore.props(), "userStore")

    whenReady(ask(userStore, AskForAirport("CDG")).mapTo[Option[UserInfo]]) {
      userInfo =>
        userInfo should not(be(defined))
    }
  }

  it("should not give airport of an unregistered user") {
    val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    val userStore = system.actorOf(UserStore.props(), "userStore")

    val answer = ask(userStore, Ask(TeamMail("xbucchiotty@xebia.fr"))).mapTo[Option[Airport]]

    whenReady(answer) {
      storedAirport =>
        storedAirport should not(be(defined))
    }
  }

}
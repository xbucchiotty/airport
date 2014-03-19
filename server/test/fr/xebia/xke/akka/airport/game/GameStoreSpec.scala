package fr.xebia.xke.akka.airport.game

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.{Address, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.util.Timeout
import language.postfixOps
import concurrent.duration._
import GameStore.{GameStarted, StartGame, GameCreated, NewGame}
import fr.xebia.xke.akka.airport.{Airport, Settings}
import fr.xebia.xke.akka.airport.plane.FullStepPlane
import akka.testkit.TestProbe

class GameStoreSpec extends FunSpec with ShouldMatchers {

  implicit val timeout = Timeout(5 seconds)

  describe("A game store") {

    it("should create a game for a user") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")
      val userInfo = UserInfo(TeamMail("xbucchiotty@xebia.fr"), Airport("Paris", "CDG", "42", "2"))

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))

      probe expectMsg GameCreated
    }

    it("should be able to start a created game") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val userId = TeamMail("xbucchiotty@xebia.fr")
      val userInfo = UserInfo(userId, Airport("Paris", "CDG", "42", "2"), Some(Address("tcp", "TestSystem")))
      val gameStore = system.actorOf(GameStore.props(), "gameStore")

      val probe = TestProbe()

      probe.send(gameStore, NewGame(userInfo, Settings.TEST, classOf[FullStepPlane]))
      probe expectMsg GameCreated

      probe.send(gameStore, StartGame(userInfo))
      probe expectMsg GameStarted
    }
  }
}
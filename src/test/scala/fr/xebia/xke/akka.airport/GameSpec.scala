package fr.xebia.xke.akka.airport

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import concurrent.duration._
import org.scalatest.{BeforeAndAfter, OneInstancePerTest, FunSpec}

class GameSpec extends FunSpec with OneInstancePerTest with BeforeAndAfter {

  private implicit val system = ActorSystem.create("GameSpec")

  describe("A game") {

    it("should terminates when runway is terminated") {
      val airControl = system.actorOf(Props[AirTrafficControl], "airControl")
      val game = system.actorOf(Props.create(classOf[Game], airControl), "game")
      val probe = TestProbe()

      probe watch game

      //When
      system.stop(airControl)

      //Then
      probe.expectTerminated(game, 100 milliseconds)
    }
  }

  after {
    system.shutdown()
  }

}


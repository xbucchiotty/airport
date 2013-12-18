package fr.xebia.xke.akka.airport

import akka.actor.{ActorSystem, Props, ActorRef}
import akka.testkit.TestProbe
import concurrent.duration._
import org.scalatest.{BeforeAndAfter, FunSpec}

class GameSpec extends FunSpec with BeforeAndAfter {

  private implicit var system: ActorSystem = null
  private implicit var game: ActorRef = null

  describe("A game") {

    it("should terminates when runway is terminated") {
      val runway = system.actorOf(Props[Runway])
      game = system.actorOf(Props.create(classOf[Game], runway))
      val probe = TestProbe()

      probe watch game

      //When
      system.stop(runway)

      //Then
      probe.expectTerminated(game, 100 milliseconds)
    }
  }


  before {
    system = ActorSystem.create("GameSpec")
  }

  after {
    system.shutdown()
  }

}


package fr.xebia.xke.akka.airport

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import concurrent.duration._
import org.scalatest.FunSpec

class GameSpec extends FunSpec {

  private implicit val system = ActorSystem.create("GameSpec")

  describe("A game") {

    it("should terminates when runway is terminated") {
      val runway = system.actorOf(Props[Runway])
      val game = system.actorOf(Props.create(classOf[Game], runway))
      val probe = TestProbe()

      probe watch game

      //When
      system.stop(runway)

      //Then
      probe.expectTerminated(game, 100 milliseconds)
    }
  }

}


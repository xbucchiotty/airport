package fr.xebia.xke.akka.airport

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.testkit.TestProbe
import concurrent.duration._

class AirportManagerSpec extends FunSpec with ShouldMatchers {

  describe("An airportManager") {

    it("should create new AirTrafficControl on demand") {
      //Given
      implicit val system = testActorSystem()
      val airportManager = system.actorOf(AirportManager.props, "airportManager")
      val probe = TestProbe()

      //When
      probe.send(airportManager, NewGameInstance("1212133--1212"))

      //Then
      val reply = probe receiveOne 500.milliseconds
      reply.isInstanceOf[GameInstance] should be(true)
    }
  }


  def testActorSystem(): ActorSystem = {
    ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
  }
}

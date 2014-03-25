package fr.xebia.xke.akka.infrastructure

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import RouteStore.{AskNewRouteTo, NewRoute, AskNewRouteFrom}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Millis, Span}
import akka.util.Timeout
import concurrent.duration._
import language.postfixOps
import fr.xebia.xke.akka.airport.Airport

class RouteStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val timeout = Timeout(5 seconds)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(100, Millis))

  describe("A coordinator") {

    it("should be able to give a flight plane from a known airport") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val coordinator = system.actorOf(RouteStore.props, "coordinator")

      val source = Airport.airports.head

      whenReady(ask(coordinator, AskNewRouteFrom(source.code)).mapTo[NewRoute]) {
        reply =>
          reply should be(defined)
          reply.get.from should equal(source.code)
      }
    }

    it("should be able to give a flight plane to a known airport") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val coordinator = system.actorOf(RouteStore.props, "coordinator")

      val source = Airport.airports.head

      whenReady(ask(coordinator, AskNewRouteTo(source.code)).mapTo[NewRoute]) {
        reply =>
          reply should be(defined)
          reply.get.to should equal(source.code)
      }
    }
  }
}

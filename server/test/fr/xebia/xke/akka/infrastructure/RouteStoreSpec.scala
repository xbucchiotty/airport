package fr.xebia.xke.akka.infrastructure

import org.scalatest.{ShouldMatchers, FunSpec}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import RouteStore.{AskNewRouteTo, NewRoute, AskNewRouteFrom}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Second, Millis, Span}
import akka.util.Timeout
import concurrent.duration._
import language.postfixOps
import fr.xebia.xke.akka.airport.{Route, AirportCode, Airport}

class RouteStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

  implicit val timeout = Timeout(1 second)

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(1, Second), interval = Span(100, Millis))

  describe("A coordinator") {

    val CDG = AirportCode("CDG")
    val CDG_HKG: Route = Route(from = CDG, to = AirportCode("HKG"), stops = 0, airline = "CX")
    val JFK_CDG: Route = Route(from = AirportCode("JFK"), to = CDG, stops = 0, airline = "AF")

    val CDGAirport = Airport(
      city = "Paris",
      code = CDG,
      latitude = "49",
      longitude = "2",
      departures = List(CDG_HKG),
      arrivals = List(JFK_CDG))

    val airports = Map(CDG -> CDGAirport)

    it("should be able to give a flight plane from an airport") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val coordinator = system.actorOf(RouteStore.props(airports), "coordinator")

      whenReady(ask(coordinator, AskNewRouteFrom(CDG)).mapTo[NewRoute]) {
        reply =>
          reply should be(defined)
          reply.get should equal(CDG_HKG)
      }
    }

    it("should be able to give a flight plane to an airport") {
      implicit val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val coordinator = system.actorOf(RouteStore.props(airports), "coordinator")

      whenReady(ask(coordinator, AskNewRouteTo(CDG)).mapTo[NewRoute]) {
        reply =>
          reply should be(defined)
          reply.get should equal(JFK_CDG)
      }
    }
  }
}

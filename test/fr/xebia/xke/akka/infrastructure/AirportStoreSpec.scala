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
import AirportStore._
import fr.xebia.xke.akka.airport.{AirportCode, Airport}
import java.util.UUID

class AirportStoreSpec extends FunSpec with ShouldMatchers with ScalaFutures {

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

    it("should be able to register to get a new airport") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportStore = system.actorOf(AirportStore.props(airports), "airportStore")

      whenReady(ask(airportStore, Register).mapTo[Registered]) {
        case Registered(airport) =>
          airport should equal(CDGAirport)
      }
    }

    it("should assign airports in order") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportStore = system.actorOf(AirportStore.props(airports), "airportStore")

      whenReady(ask(airportStore, Register).mapTo[Registered]) {
        case Registered(firstAirport) =>

          firstAirport should equal(CDGAirport)

          whenReady(ask(airportStore, Register).mapTo[Registered]) {
            case Registered(secondAirport) =>

              secondAirport should equal(JFKAirport)
          }
      }
    }

    it("should tell airport is bound") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportStore = system.actorOf(AirportStore.props(airports), "airportStore")

      whenReady(ask(airportStore, Register).mapTo[Registered]) {
        case Registered(airport) =>

          whenReady(ask(airportStore, AirportStore.IsRegistered(airport.code)).mapTo[Option[Airport]]) {
            reply =>
              reply should be(defined)
          }
      }
    }

    it("should tell airport is not bound") {
      val system = ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
      val airportStore = system.actorOf(AirportStore.props(airports), "airportStore")

      whenReady(ask(airportStore, AirportStore.IsRegistered(AirportCode("CDG"))).mapTo[Option[Airport]]) {
        reply =>
          reply should not(be(defined))
      }
    }
  }
}
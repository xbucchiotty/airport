package fr.xebia.xke.akka.infrastructure

import akka.actor.{Props, Actor}
import scala.util.Random
import fr.xebia.xke.akka.airport.{AirportCode, Route, Airport}
import fr.xebia.xke.akka.infrastructure.RouteStore.{AskNewRouteTo, AskNewRouteFrom}

class RouteStore extends Actor {

  var airports: Map[AirportCode, Airport] = _

  override def preStart() {
    airports = Airport.airports.groupBy(_.code).mapValues(_.head)
  }

  def receive: Receive = {

    case AskNewRouteFrom(source) =>
      findRouteFrom(source)

    case AskNewRouteTo(target) =>
      findRouteTo(target)
  }

  def findRouteFrom(source: AirportCode) {
    val route: Option[Route] = airports.get(source).map(airport => {
      val numberOfDepartureFlights = airport.departures.size

      val chooseOneDepartureFlight = Random.nextInt(numberOfDepartureFlights)

      airport.departures(chooseOneDepartureFlight)
    })

    sender ! route
  }

  def findRouteTo(target: AirportCode) {
    val route: Option[Route] = airports.get(target).map(airport => {
      val numberOfArrivalFlights = airport.arrivals.size

      val chooseOneArrivalFlight = Random.nextInt(numberOfArrivalFlights)

      airport.arrivals(chooseOneArrivalFlight)
    })

    sender ! route
  }
}

object RouteStore {

  def props = Props[RouteStore]

  case class AskNewRouteFrom(from: AirportCode)

  case class AskNewRouteTo(target: AirportCode)

  type NewRoute = Option[Route]
}

package fr.xebia.xke.akka.infrastructure

import akka.actor.{Props, Actor}
import scala.util.Random
import fr.xebia.xke.akka.airport.{Route, Airport}
import scala.Predef._
import fr.xebia.xke.akka.airport.AirportCode
import fr.xebia.xke.akka.infrastructure.RouteStore.AskNewRouteFrom
import fr.xebia.xke.akka.infrastructure.RouteStore.AskNewRouteTo

class RouteStore(airports: Map[AirportCode, Airport]) extends Actor {

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

  def props(airports: Map[AirportCode, Airport]) = Props(classOf[RouteStore], airports)

  case class AskNewRouteFrom(from: AirportCode)

  case class AskNewRouteTo(target: AirportCode)

  type NewRoute = Option[Route]
}

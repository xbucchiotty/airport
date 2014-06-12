package fr.xebia.xke.akka.plane.event

import akka.actor.ActorRef

case class PlaneStatus(step: String, flightName: String, detail: String, error: String)

object PlaneStatus {

  def empty(plane: ActorRef): PlaneStatus = new PlaneStatus("incoming", plane.path.name, "Hello, there!", "")
}
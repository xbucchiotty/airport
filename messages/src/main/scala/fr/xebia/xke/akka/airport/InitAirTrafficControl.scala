package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class InitAirTrafficControl(runways: Seq[ActorRef], ackMaxDuration: Int)

case object AirTrafficControlReady
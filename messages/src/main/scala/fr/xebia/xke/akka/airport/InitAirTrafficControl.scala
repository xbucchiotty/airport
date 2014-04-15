package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class InitAirTrafficControl(groundControl: ActorRef, runways: Set[ActorRef], ackMaxDuration: Int)

case object AirTrafficControlReady

package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class InitAirTrafficControl(groundControl: ActorRef, runways: java.util.Set[ActorRef], ackMaxDuration: Int)

case object AirTrafficControlReady

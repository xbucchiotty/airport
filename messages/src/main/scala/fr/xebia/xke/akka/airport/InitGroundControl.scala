package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class InitGroundControl(taxiways: java.util.Set[ActorRef], gates: java.util.Set[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int)

case object GroundControlReady
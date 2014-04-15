package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class InitGroundControl(taxiways: Set[ActorRef], gates: Set[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int)

case object GroundControlReady
package fr.xebia.xke.akka.airport

import akka.actor.ActorRef

case class InitGroundControl(taxiways: Seq[ActorRef], gates: Seq[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int)

case object GroundControlReady
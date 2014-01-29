package controllers

import akka.actor.{Props, ActorSystem}
import fr.xebia.xke.akka.airport.PlayerWatcher
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.Cluster

trait AirportActorSystem {

  val airportActorSystem = {
    val system = ActorSystem.create("airportSystem")
    val playerWatcher = system.actorOf(Props[PlayerWatcher], "playerWatcher")
    Cluster(system).subscribe(playerWatcher, classOf[ClusterDomainEvent])
    system
  }
}

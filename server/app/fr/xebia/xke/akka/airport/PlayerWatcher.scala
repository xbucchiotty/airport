package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, Actor}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, MemberRemoved, UnreachableMember, MemberUp, CurrentClusterState}

class PlayerWatcher extends Actor with ActorLogging {

  def receive: Receive = {
    case state: CurrentClusterState =>

    case MemberUp(member) =>
      if (member.roles.contains("player")) {
        controllers.Application.playerOnline(member.address)

      }
    case UnreachableMember(member) =>
      controllers.Application.playerOffline(member.address)

    case MemberRemoved(member, previousStatus) =>
      controllers.Application.playerOffline(member.address)

    case _: ClusterDomainEvent => // ignore
  }
}

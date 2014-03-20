package fr.xebia.xke.akka.airport.game

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, MemberRemoved, UnreachableMember, MemberUp, CurrentClusterState}

class PlayerWatcher(playerStore: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case state: CurrentClusterState =>

    case MemberUp(member) if member.roles.contains("player") =>
      playerStore ! PlayerStore.BindActorSystem(member.address, member.roles)

    case UnreachableMember(member) if member.roles.contains("player") =>
      log.warning(s"player $member moved out")

    case MemberRemoved(member, previousStatus) if member.roles.contains("player") =>
      log.warning(s"player $member moved out")
      playerStore ! PlayerStore.UnbindActorSystem(member.address, member.roles)

    case _: ClusterDomainEvent => // ignore
  }
}

object PlayerWatcher {
  def props(playerStore: ActorRef): Props = Props(classOf[PlayerWatcher], playerStore)
}
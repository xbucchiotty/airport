package fr.xebia.xke.akka.airport.game

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.cluster.ClusterEvent.{MemberRemoved, UnreachableMember, MemberUp, CurrentClusterState}
import akka.cluster.{Member, MemberStatus}

class PlayerWatcher(playerStore: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case state: CurrentClusterState =>
      state.members
        .filter(_.status == MemberStatus.Up)
        .filter(memberIsPlayer)
        .foreach(member => {
        log.info(s"player $member moves in")
        playerStore ! PlayerStore.BindActorSystem(member.address, member.roles)
      })

    case MemberUp(member) if memberIsPlayer(member) =>
      log.warning(s"player $member moves in")
      playerStore ! PlayerStore.BindActorSystem(member.address, member.roles)

    case UnreachableMember(member) if memberIsPlayer(member) =>
      log.warning(s"player $member moved out")

    case MemberRemoved(member, previousStatus) if memberIsPlayer(member) =>
      log.warning(s"player $member moved out")
      playerStore ! PlayerStore.UnbindActorSystem(member.address, member.roles)

  }

  val memberIsPlayer = (member: Member) => member.roles.contains("player")

}

object PlayerWatcher {
  def props(playerStore: ActorRef): Props = Props(classOf[PlayerWatcher], playerStore)
}
package fr.xebia.xke.akka.plane

import akka.actor.{Props, ActorRef, Actor}
import akka.event.EventStream
import fr.xebia.xke.akka.plane.event.{DetailChanged, StateChanged, ErrorHappened, PlaneStatus}

case class PlaneListener(plane: ActorRef, gameStream: EventStream) extends Actor {

  def updateStatus(currentStatus: PlaneStatus): Receive = {

    case ErrorHappened(message) =>
      val newStatus = currentStatus.copy(error = message)

      gameStream publish newStatus

      context.become(updateStatus(newStatus))

    case StateChanged(newStep) if newStep != currentStatus.step =>
      val newStatus = currentStatus.copy(step = newStep)

      gameStream publish newStatus

      context.become(updateStatus(newStatus))


    case DetailChanged(value) if value != currentStatus.detail =>
      val newStatus = currentStatus.copy(detail = value)

      gameStream publish newStatus

      context.become(updateStatus(newStatus))

  }

  def receive = updateStatus(PlaneStatus.empty(plane))

  override def preStart() {
    gameStream publish PlaneStatus.empty(plane)
  }
}

object PlaneListener {

  def props(plane: ActorRef, gameStream: EventStream) =
    Props(classOf[PlaneListener], plane, gameStream)
}
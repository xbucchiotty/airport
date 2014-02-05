package fr.xebia.xke.akka.airport.plane

import akka.actor.{ActorRef, Actor}
import akka.event.EventStream
import controllers.PlaneStatus
import fr.xebia.xke.akka.airport.plane.event.{DetailChanged, StateChanged, ErrorHappened}

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

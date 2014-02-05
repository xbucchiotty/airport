package fr.xebia.xke.akka.airport.plane.state

import akka.actor.{ActorRef, Cancellable, Actor}
import fr.xebia.xke.akka.airport.command.Ack
import fr.xebia.xke.akka.airport.Settings

private[plane] trait RadioCommunication extends Actor {

  def settings: Settings

  private var lastReply: Cancellable = null

  def replyWithRadio(to: ActorRef)(reply: Transition) {
    if (settings.isRadioOk) {

      import context.dispatcher
      lastReply = context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          to ! Ack

          reply()
        }
      })
    }
  }

  override def postStop() {
    super.postStop()
    if (lastReply != null && !lastReply.isCancelled) {
      lastReply.cancel()
    }
  }

}
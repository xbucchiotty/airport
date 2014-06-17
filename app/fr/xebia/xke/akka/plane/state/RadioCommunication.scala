package fr.xebia.xke.akka.plane.state

import akka.actor.{Cancellable, Actor}
import fr.xebia.xke.akka.game.Settings
import fr.xebia.xke.akka.Transition
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack

private[plane] trait RadioCommunication {

  this: Actor =>

  def settings: Settings

  private var lastReply: Cancellable = null

  def replyWithRadio(transition: Transition) {
    if (settings.isRadioOk) {
      val operator = sender()
      import context.dispatcher
      lastReply = context.system.scheduler.scheduleOnce(settings.aRandomAckDuration, new Runnable {
        def run() {
          operator ! Ack

          transition()
        }
      })
    }
  }

  override def postStop() {
    if (lastReply != null && !lastReply.isCancelled) {
      lastReply.cancel()
    }
  }

}
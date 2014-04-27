package fr.xebia.xke.akka.infrastructure

import akka.actor._
import scala.collection.immutable.Queue
import akka.event.EventStream
import controllers.DequeueEvents
import scala.Predef._
import fr.xebia.xke.akka.game._
import fr.xebia.xke.akka.plane.event.PlaneStatus
import fr.xebia.xke.akka.game.PlayerDown
import fr.xebia.xke.akka.game.Score
import fr.xebia.xke.akka.game.PlayerUp
import scala.Some

class EventListener(eventStream: EventStream) extends Actor with ActorLogging {

  private var buffer = Queue.empty[String]
  private var listening = true

  private var pendingRequest: Option[ActorRef] = None

  override def preStart() {
    eventStream.subscribe(self, classOf[GameEvent])
    eventStream.subscribe(self, classOf[PlaneStatus])
  }

  override def postStop() {
    eventStream.unsubscribe(self)
  }

  def receive = {
    case status: PlaneStatus =>
      sendWhenPendingRequestOrQueue(toJson(status))

    case GameOver =>
      sendWhenPendingRequestOrQueue(gameOver)
      listening = false
      eventStream.unsubscribe(self)

    case GameEnd =>
      sendWhenPendingRequestOrQueue(gameEnd)
      listening = false
      eventStream.unsubscribe(self)

    case newScore: Score =>
      sendWhenPendingRequestOrQueue(score(newScore))

    case PlayerUp(address) =>
      sendWhenPendingRequestOrQueue(playerUp(address))

    case PlayerDown(address) =>
      sendWhenPendingRequestOrQueue(playerDown(address))

    case DequeueEvents =>
      if (buffer.nonEmpty) {
        val (msg, newBuffer) = buffer.dequeue
        sender() ! Some(msg)
        log.debug(s"send $msg")
        buffer = newBuffer
      } else {
        if (!listening) {
          log.debug(s"end of stream")
          sender() ! Option.empty[String]
        }
        else {
          log.debug(s"pending request")
          pendingRequest = Some(sender())
        }
      }
  }

  def sendWhenPendingRequestOrQueue(msg: String) {
    pendingRequest match {
      case None =>
        log.debug(s"Queuing")
        buffer = buffer enqueue msg

      case Some(caller) =>
        log.debug(s"Send $msg")
        caller ! Some(msg)
        pendingRequest = None
    }
  }

  def toJson(planeStatus: PlaneStatus): String = {
    import planeStatus._

    s"""{
   "type" : "PlaneStatus" ,
   "step" : "${step.toLowerCase}" ,
   "flightName" : "$flightName" ,
   "detail" : "$detail" ,
   "error" : "$error"
   }""".stripMargin
  }

  def gameOver: String =
    s"""{
      "type" : "GameOver"
   }""".stripMargin

  def gameEnd: String =
    s"""{
      "type" : "GameEnd"
   }""".stripMargin

  def score(score: Score): String = {
    import score._
    s"""{
      "type" : "Score" ,
      "current": "$current",
      "objective": "$objective"
   }""".stripMargin
  }

  def playerUp(address: Address): String = {
    s"""{
      "type" : "PlayerUp",
      "address" : "$address"
    }""".stripMargin
  }

  def playerDown(address: Address): String = {
    s"""{
      "type" : "PlayerDown",
      "address" : "$address"
    }""".stripMargin
  }
}

object EventListener {
  def props(eventStream: EventStream): Props = Props(classOf[EventListener], eventStream)
}
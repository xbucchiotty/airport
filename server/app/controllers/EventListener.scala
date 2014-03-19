package controllers

import akka.actor.{Props, Address, Actor}
import fr.xebia.xke.akka.airport._
import scala.collection.immutable.Queue
import fr.xebia.xke.akka.airport.Score
import fr.xebia.xke.akka.airport.PlayerUp
import scala.Some
import akka.event.EventStream

class EventListener(eventStream: EventStream) extends Actor {

  private var buffer = Queue.empty[String]

  override def preStart() {
    eventStream.subscribe(self, classOf[GameEvent])
    eventStream.subscribe(self, classOf[PlaneStatus])
  }

  override def postStop() {
    eventStream.unsubscribe(self)
  }

  def receive = {
    case status: PlaneStatus =>
      buffer = buffer enqueue toJson(status)

    case GameOver =>
      buffer = buffer enqueue gameOver
      eventStream.unsubscribe(self)

    case GameEnd =>
      buffer = buffer enqueue gameEnd
      eventStream.unsubscribe(self)

    case newScore: Score =>
      buffer = buffer enqueue score(newScore)

    case PlayerUp(_, address) =>
      buffer = buffer enqueue playerUp(address)

    case PlayerDown(_, address) =>
      buffer = buffer enqueue playerDown(address)

    case DequeueEvents =>
      if (buffer.nonEmpty) {
        val (msg, newBuffer) = buffer.dequeue
        sender ! Some(msg)
        buffer = newBuffer
      } else {
        sender ! Option.empty[String]
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
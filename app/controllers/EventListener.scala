package controllers

import akka.actor.Actor
import fr.xebia.xke.akka.airport.{Score, GameOver}
import scala.collection.immutable.Queue

class EventListener extends Actor {

  private var buffer = Queue.empty[String]

  def receive = {
    case status: PlaneStatus =>
      buffer = buffer enqueue toJson(status)

    case GameOver =>
      buffer = buffer enqueue gameOver
      context.system.eventStream.unsubscribe(self)

    case newScore: Score =>
      buffer = buffer enqueue score(newScore)

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
      "type" : "GameOver" ,
   }""".stripMargin

  def score(score: Score): String = {
    import score._
    s"""{
      "type" : "Score" ,
      "current": "$current",
      "objective": "$objective"
   }""".stripMargin
  }

}
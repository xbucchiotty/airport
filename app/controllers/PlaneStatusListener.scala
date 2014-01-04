package controllers

import akka.actor.Actor
import fr.xebia.xke.akka.airport.PlaneEvent
import scala.collection.immutable.Queue

class PlaneStatusListener extends Actor {

  private var buffer = Queue.empty[String]

  def receive = {
    case status: PlaneStatus =>
      buffer = buffer enqueue toJson(status)

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
   "step" : "${step.toLowerCase}" ,
   "flightName" : "$flightName" ,
   "detail" : "$detail"
   }""".stripMargin
  }

}
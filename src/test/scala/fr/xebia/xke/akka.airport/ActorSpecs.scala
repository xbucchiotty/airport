package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import org.scalatest.FreeSpec
import concurrent.duration._

trait ActorSpecs extends FreeSpec {

  def `Given an actor system`(fun: (ActorSystem => Unit)) {
    fun {
      ActorSystem()
    }
  }

  def `Then it should terminates`(probe: TestProbe, target: ActorRef) {
    "Then it should terminates " in {

      probe expectTerminated target
    }
  }

  def `Then nothing should happen`(probe: TestProbe, target: ActorRef) {
    "Then nothing should happen " in {

      probe.expectNoMsg(10 milliseconds)
    }
  }

}

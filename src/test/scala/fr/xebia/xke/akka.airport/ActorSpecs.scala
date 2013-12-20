package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import org.scalatest.FreeSpec
import concurrent.duration._
import languageFeature.postfixOps

trait ActorSpecs extends FreeSpec {

  def `Given an actor system`(fun: (ActorSystem => NextStep)) {
    fun {
      ActorSystem()
    }
  }

  def `Given a probe watching`(target: ActorRef)(fun: (TestProbe => NextStep))(implicit system: ActorSystem) {
    `Given a probe` {
      probe =>
        probe watch target
        fun(probe)
    }
  }

  def `Given a probe`(fun: (TestProbe => NextStep))(implicit system: ActorSystem) {
    fun {
      TestProbe()
    }
  }

  def `When target terminates`(target: ActorRef)(fun: => NextStep)(implicit system: ActorSystem) {
    s"When the ${target.path.name} terminates" - {

      system stop target

      fun
    }
  }

  def `Then it should terminates`(probe: TestProbe, target: ActorRef) {
    s"Then the ${target.path.name} should terminates " in {

      probe expectTerminated target
    }
  }

  def `Then nothing should happen`(probe: TestProbe, target: ActorRef) {
    s"Then nothing should happen to the ${target.path.name} " in {

      probe.expectNoMsg(10 milliseconds)
    }
  }

}

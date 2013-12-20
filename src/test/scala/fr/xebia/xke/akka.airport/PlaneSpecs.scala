package fr.xebia.xke.akka.airport

import akka.actor.{ActorSystem, ActorRef}
import akka.testkit.TestProbe
import fr.xebia.xke.akka.airport.Event.Parked
import org.scalatest.FreeSpec

trait PlaneSpecs extends FreeSpec {
  def `When a plane parks at`(gate: ActorRef)(fun: => Unit)(implicit system: ActorSystem) {
    "When a plane parks " - {

      TestProbe().send(gate, Parked)

      fun
    }
  }

}

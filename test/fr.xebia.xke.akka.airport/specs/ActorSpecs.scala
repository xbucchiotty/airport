package fr.xebia.xke.akka.airport.specs

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport._
import languageFeature.postfixOps
import org.scalatest.FreeSpec

trait ActorSpecs extends FreeSpec {

  def `Given an actor system`(fun: (ActorSystem => NextStep)) {
    fun {
      ActorSystem()
    }
  }

}

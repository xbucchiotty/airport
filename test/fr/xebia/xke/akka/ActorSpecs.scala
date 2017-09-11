package fr.xebia.xke.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.FreeSpec

import scala.languageFeature.postfixOps

trait ActorSpecs extends FreeSpec {

  def `Given an actor system`(fun: (ActorSystem => NextStep)) {
    fun {
      ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    }
  }

}

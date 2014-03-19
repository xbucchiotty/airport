package fr.xebia.xke.akka.airport.specs

import akka.actor.ActorSystem
import languageFeature.postfixOps
import org.scalatest.FreeSpec
import com.typesafe.config.ConfigFactory
import fr.xebia.xke.akka.NextStep

trait ActorSpecs extends FreeSpec {

  def `Given an actor system`(fun: (ActorSystem => NextStep)) {
    fun {
      ActorSystem("TestSystem", ConfigFactory.load("application-test.conf"))
    }
  }

}

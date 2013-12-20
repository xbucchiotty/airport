package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import fr.xebia.xke.akka.airport._

trait GateSpecs extends ActorSpecs {

  def `Given a gate`(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a gate" - {

      fun {
        system.actorOf(Props[Gate], "gate")
      }
    }

  }

}

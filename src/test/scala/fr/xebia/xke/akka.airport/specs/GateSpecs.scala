package fr.xebia.xke.akka.airport.specs

import akka.actor.{Props, ActorSystem, ActorRef}
import fr.xebia.xke.akka.airport.NextStep
import fr.xebia.xke.akka.airport.Gate

trait GateSpecs extends ActorSpecs {

  def `Given a gate`(groundControl: ActorRef)(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a gate" - {

      fun {
        system.actorOf(Props(classOf[Gate], groundControl), "gate")
      }
    }

  }

}

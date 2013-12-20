package fr.xebia.xke.akka.airport

import akka.actor.{Props, ActorSystem, ActorRef}

trait GateSpecs extends ActorSpecs {

  def `Given a gate`(fun: (ActorRef => NextStep))(implicit system: ActorSystem) {
    "Given a gate" - {

      fun {
        system.actorOf(Props[Gate], "gate")
      }
    }

  }

}

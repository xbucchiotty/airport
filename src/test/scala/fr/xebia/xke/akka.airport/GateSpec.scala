package fr.xebia.xke.akka.airport

import languageFeature.postfixOps

class GateSpec extends GateSpecs with PlaneSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a gate` {
        (gate, probe) =>

          `Given a plane has already parked at`(gate) {

            `When a plane parks at`(gate) {

              `Then it should terminates`(probe, gate)
            }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a gate` {
        (gate, probe) =>

          `When a plane parks at`(gate) {

            `Then nothing should happen`(probe, gate)
          }
      }
  }
}
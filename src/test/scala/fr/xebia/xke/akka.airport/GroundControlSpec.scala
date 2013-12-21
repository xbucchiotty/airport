package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{GroundControlSpecs, ActorSpecs}

class GroundControlSpec extends GroundControlSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        plane =>

          `Given a probe watching`(plane.ref) {
            probe =>

              `Given a ground control` {
                groundControl =>

                  `When a plane incomes`(plane, groundControl) {

                    `Then it should tell the plane to park`(plane)

                  }
              }
          }
      }
  }
}
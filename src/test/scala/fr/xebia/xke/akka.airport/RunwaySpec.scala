package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{RunwaySpecs, ActorSpecs, PlaneSpecs, AirTrafficControlSpecs}

class RunwaySpec extends RunwaySpecs with ActorSpecs with PlaneSpecs with AirTrafficControlSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        airControl =>

          `Given a probe` {
            plane =>

              `Given a runway`(airControl.ref) {
                runway =>

                  `Given a probe watching`(runway) {
                    probe =>

                      `When the plane lands at`(plane, runway) {

                        `Then air traffic control is notified of the landing`(airControl, plane.ref, runway)
                      }
                  }
              }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        airControl =>

          `Given a runway`(airControl.ref) {
            runway =>

              `Given a probe watching`(runway) {
                probe =>

                  `Given a plane has already landed`(runway) {

                    `When a plane lands at`(runway) {

                      `Then it should terminates`(probe, runway)

                    }
                  }
              }
          }
      }
  }
}
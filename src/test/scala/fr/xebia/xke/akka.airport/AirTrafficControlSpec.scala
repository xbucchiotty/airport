package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{RunwaySpecs, PlaneSpecs, AirTrafficControlSpecs}


class AirTrafficControlSpec extends AirTrafficControlSpecs with PlaneSpecs with RunwaySpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        groundControl =>

          `Given an air traffic control`(groundControl.ref) {
            control =>

              `Given a runway`(control) {
                runway =>

                  `When a plane incomes`(control) {
                    plane =>

                      `Then it should tell the plane to land`(plane)

                      `When the plane lands at`(plane, runway) {

                        `Then air traffic control should tell the plane to contact ground control`(plane, groundControl.ref)
                      }
                  }
              }
          }
      }
  }
}
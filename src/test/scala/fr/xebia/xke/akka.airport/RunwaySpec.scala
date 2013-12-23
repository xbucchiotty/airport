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

                  `When the plane lands at`(plane, runway) {

                    `Then air traffic control is notified of the landing`(airControl, plane.ref, runway)

                    `When the plane leaves`(plane, runway) {
                      `Then air traffic control is notified of the plane leaving the runway`(airControl, plane.ref, runway)

                      `Given a probe` {
                        secondPlane =>

                          `When the plane lands at`(secondPlane, runway) {

                            `Then air traffic control is notified of the landing`(airControl, secondPlane.ref, runway)
                          }
                      }
                    }
                  }
              }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        firstPlane =>

          `Given a probe` {
            secondPlane =>

              `Given a probe` {
                airControl =>

                  `Given a runway`(airControl.ref) {
                    runway =>

                      `Given a probe watching`(runway) {
                        probe =>

                          `Given a plane has already landed`(firstPlane, runway) {

                            `When a plane lands at`(secondPlane, runway) {

                              `Then it should terminates`(probe, runway)

                            }
                          }
                      }
                  }
              }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        plane =>

          `Given a probe` {
            airControl =>

              `Given a runway`(airControl.ref) {
                runway =>

                  `Given a probe watching`(runway) {
                    probe =>

                      `When the plane leaves`(plane, runway) {

                        `Then it should terminates`(probe, runway)

                      }
                  }
              }
          }
      }
  }
}
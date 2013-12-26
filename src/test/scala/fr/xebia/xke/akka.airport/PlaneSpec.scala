package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{PlaneSpecs, ActorSpecs}

class PlaneSpec extends PlaneSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a flying plane` {
        plane =>

          `Given a probe` {
            runway =>

              `Given a probe` {
                airControl =>

                  `When a plane is requested to land`(airControl, plane, runway.ref) {

                    `Then the plane should land within timeout`(plane, runway)

                  }
              }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a flying plane` {
        plane =>

          `Given a probe watching`(plane) {
            probe =>

              `When a plane is not requested to land withing timeout` {

                `Then it should terminates`(probe, plane)

              }
          }
      }
  }

  `Given an actor system` {
    implicit system =>

      `Given a flying plane` {
        plane =>

          `Given a probe` {
            runway =>

              `Given a plane has already landed`(plane, runway.ref) {

                `Given a probe` {
                  gate =>

                    `Given a probe` {
                      taxiway =>

                        `Given a probe` {
                          groundControl =>

                            `When a plane is requested to taxi to gate through taxiway`(groundControl, plane, taxiway.ref, gate.ref) {

                              `Then plane should leave runway`(plane, runway)

                              `Then plane should enter the taxiway`(plane, taxiway, gate.ref)

                              `Given a probe watching`(plane) {
                                probe =>

                                  `When a plane parks at`(plane, gate.ref) {

                                    `Then it should terminates`(probe, plane)
                                  }
                              }
                            }
                        }
                    }
                }
              }
          }
      }
  }
}
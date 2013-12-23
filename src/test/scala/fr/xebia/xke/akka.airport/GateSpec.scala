package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{GroundControlSpecs, PlaneSpecs, ActorSpecs, GateSpecs}

class GateSpec extends GateSpecs with PlaneSpecs with ActorSpecs with GroundControlSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        groundControl =>

          `Given a probe` {
            firstPlane =>

              `Given a gate`(groundControl.ref) {
                gate =>

                  `Given a probe watching`(gate) {
                    probe =>

                      `When a plane parks at`(firstPlane, gate) {

                        `Then ground control is notified of the plane parked at gate`(groundControl, firstPlane.ref, gate)

                        `When the plane leaves`(firstPlane, gate) {

                          `Then ground control is notified of the plane leaving gate`(groundControl, firstPlane.ref, gate)

                          `Given a probe` {
                            secondPlane =>

                              `When a plane parks at`(secondPlane, gate) {

                                `Then ground control is notified of the plane parked at gate`(groundControl, secondPlane.ref, gate)

                              }
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
            groundControl => {

              `Given a gate`(groundControl.ref) {
                gate =>

                  `Given a probe watching`(gate) {
                    probe =>

                      `Given a plane has already parked at`(gate) {

                        `When a plane parks at`(plane, gate) {

                          `Then it should terminates`(probe, gate)
                        }
                      }
                  }
              }
            }
          }
      }
  }
}
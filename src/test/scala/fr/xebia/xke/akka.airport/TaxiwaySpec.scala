package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{TaxiwaySpecs, PlaneSpecs, ActorSpecs}

class TaxiwaySpec extends TaxiwaySpecs with PlaneSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        groundControl => {

          `Given a taxiway of capacity`(1, groundControl.ref) {
            taxiway =>

              `Given a probe` {
                plane =>

                  `When a plane enters the taxiway`(plane, taxiway) {

                    `Then ground control is notified of the plane entering the taxiway`(groundControl, plane.ref)
                  }
              }

              `Given a probe watching`(taxiway) {
                probe =>

                  `Given a plane has already entered the taxiway`(taxiway) {

                    `Given a probe` {
                      plane =>

                        `When a plane enters the taxiway`(plane, taxiway) {

                          `Then it should terminates`(probe, taxiway)
                        }
                    }
                  }
              }
          }
        }
      }
  }
}
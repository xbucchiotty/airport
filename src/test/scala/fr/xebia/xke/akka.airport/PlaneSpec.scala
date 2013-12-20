package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{PlaneSpecs, ActorSpecs}

class PlaneSpec extends PlaneSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a flying plane` {
        plane =>

          `Given a probe` {
            runway =>

              `When a plane is requested to land`(plane, runway.ref) {

                `Then the plane should land within timeout`(plane, runway)
              }
          }
      }
  }
}
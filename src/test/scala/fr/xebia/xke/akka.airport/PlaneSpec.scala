package fr.xebia.xke.akka.airport

class PlaneSpec extends PlaneSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a flying plane` {
        plane =>

          `Given a probe` {
            runway =>

              `When a plane requested to land`(plane, runway.ref) {

                `Then the plane should land within timeout`(plane, runway)
              }
          }
      }
  }
}
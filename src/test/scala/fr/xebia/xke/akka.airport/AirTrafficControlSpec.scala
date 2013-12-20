package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.AirTrafficControlSpecs


class AirTrafficControlSpec extends AirTrafficControlSpecs {

  `Given an actor system` {
    implicit system =>

      `Given an air traffic control` {
        control =>

          `When a plane incomes`(control) {
            plane =>

              `Then it should tell the plane to land`(plane)
          }
      }

  }
}


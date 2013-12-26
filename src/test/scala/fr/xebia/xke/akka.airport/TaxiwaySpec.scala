package fr.xebia.xke.akka.airport

import akka.testkit.TestProbe
import concurrent.duration._
import fr.xebia.xke.akka.airport.Event.{HasParked, HasEntered, HasLeft}
import fr.xebia.xke.akka.airport.specs.{TaxiwaySpecs, PlaneSpecs, ActorSpecs}
import languageFeature.postfixOps

class TaxiwaySpec extends TaxiwaySpecs with PlaneSpecs with ActorSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        groundControl => {

          `Given a probe` {
            gate =>

              `Given a taxiway of capacity`(1, groundControl.ref) {
                taxiway =>

                  `Given a probe` {
                    plane =>

                      `When a plane enters the taxiway`(plane, taxiway, gate.ref) {

                        `Then ground control is notified of the plane entering the taxiway`(groundControl, plane.ref, taxiway)

                        `When queuing timeout is reached`(taxiway, plane.ref, gate.ref) {

                          `Then ground control is notified of the plane leaving the taxiway`(groundControl, plane.ref, taxiway)
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
        groundControl => {

          `Given a probe` {
            gate =>

              `Given a taxiway of capacity`(1, groundControl.ref) {
                taxiway =>

                  `Given a probe` {
                    plane =>

                      `Given a probe watching`(taxiway) {
                        probe =>

                          `Given a probe` {
                            secondPlane =>

                              `When a plane enters the taxiway`(plane, taxiway, gate.ref) {

                                `When a plane enters the taxiway`(secondPlane, taxiway, gate.ref) {

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
  }

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        groundControl => {

          `Given a probe` {
            gate =>

              `Given a taxiway of capacity`(10, groundControl.ref) {
                taxiway =>

                  `Given a probe` {
                    plane =>

                      `When a plane enters the taxiway`(plane, taxiway, gate.ref) {

                        `Then plane should be out of taxiway within timeout`(taxiway, groundControl, gate, plane.ref)
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
        groundControl => {

          `Given a probe` {
            gate =>

              `Given a taxiway of capacity`(10, groundControl.ref) {
                taxiway =>

                  "Given 5 planes" - {

                    val planes = for (i <- 0 until 5)
                    yield TestProbe()

                    "When the start taxiing in an order" - {
                      planes.foreach {
                        plane =>
                          plane send(taxiway, Event.TaxiingToGate(gate.ref))
                      }

                      "Then they should leave the taxiway in the same order" in {
                        groundControl.within(10 seconds) {}
                        planes.foreach {
                          plane =>
                            groundControl expectMsg HasEntered
                        }

                        planes.foreach {
                          plane =>
                            groundControl expectMsg HasParked
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
package fr.xebia.xke.akka.airport

import fr.xebia.xke.akka.airport.specs.{GameSpecs, AirTrafficControlSpecs}


class GameSpec extends GameSpecs with AirTrafficControlSpecs {

  `Given an actor system` {
    implicit system =>

      `Given a probe` {
        groundControl => {

          `Given an air traffic control`(groundControl.ref) {
            control =>

              `Given a game`(control) {
                game =>

                  `Given a probe watching`(game) {
                    probe =>

                      `When target terminates`(control) {

                        `Then it should terminates`(probe, game)
                      }
                  }
              }
          }
        }
      }
  }
}
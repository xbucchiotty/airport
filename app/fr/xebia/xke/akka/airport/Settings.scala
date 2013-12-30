package fr.xebia.xke.akka.airport

import scala.concurrent.duration._
import scala.util.Random

class Settings(
                val nrOfRunways: Int = 1,
                val taxiwayCapacity: Int = 10,
                val nrOfGates: Int = 1,
                landingMaxDuration: Int = 300,
                taxiingMaxDuration: Int = 3000,
                unloadingPassengersMaxDuration: Int = 5000,
                val outOfKerozenTimeout: Int = 3000) {

  private def aRandomDuration(maxDurationInMillis: Int): FiniteDuration = {
    val minDuration = maxDurationInMillis / 2
    val random = Random.nextInt(minDuration)

    Duration(
      random + minDuration,
      MILLISECONDS
    )
  }

  def aLandingDuration =
    aRandomDuration(landingMaxDuration)

  def anUnloadingPassengersDuration =
    aRandomDuration(unloadingPassengersMaxDuration)

  def aRandomTaxiingDuration =
    aRandomDuration(taxiingMaxDuration)

}

object Settings {
  lazy val EASY = new Settings(1, 10, 1, 3000, 5000, 10000)
}

package fr.xebia.xke.akka.airport

import scala.concurrent.duration._
import scala.util.Random

case class Settings(
                     nrOfRunways: Int = 1,
                     landingMaxDuration: Int = 3000,
                     nrOfTaxiways: Int = 1,
                     taxiingDuration: Int = 1500,
                     taxiwayCapacity: Int = 1,
                     nrOfGates: Int = 1,
                     unloadingPassengersMaxDuration: Int = 5000,
                     outOfKerozenTimeout: Int = 10000,
                     ackMaxDuration: Int = 500,
                     radioReliability: Double = 1,
                     objective: Int = 20,
                     planeGenerationInterval: Int = 8500) {

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

  def aRandomAckDuration =
    aRandomDuration(ackMaxDuration)

  def isRadioOk =
    Random.nextDouble() <= radioReliability

}

object Settings {

  lazy val TEST = new Settings(
    nrOfRunways = 1,
    nrOfTaxiways = 1,
    taxiingDuration = 10,
    taxiwayCapacity = 1,
    nrOfGates = 1,
    landingMaxDuration = 300,
    unloadingPassengersMaxDuration = 300,
    outOfKerozenTimeout = 500,
    ackMaxDuration = 300,
    radioReliability = 1,
    objective = 20,
    planeGenerationInterval = 300
  )
}

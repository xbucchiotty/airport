package fr.xebia.xke.akka.airport

import scala.concurrent.duration._
import scala.util.Random

case class Settings(
                     nrOfRunways: Int,
                     taxiwayCapacity: Int,
                     nrOfGates: Int,
                     landingMaxDuration: Int,
                     taxiingMaxDuration: Int,
                     unloadingPassengersMaxDuration: Int,
                     outOfKerozenTimeout: Int,
                     ackMaxDuration: Int,
                     radioFability: Double) {

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

  def aRandomAckDuration =
    aRandomDuration(ackMaxDuration)

  def isRadioOk =
    Random.nextDouble() <= radioFability

}

object Settings {
  lazy val EASY = new Settings(
    nrOfRunways = 1,
    taxiingMaxDuration = 10,
    taxiwayCapacity = 1,
    nrOfGates = 1,
    landingMaxDuration = 3000,
    unloadingPassengersMaxDuration = 5000,
    outOfKerozenTimeout = 10000,
    ackMaxDuration = 500,
    radioFability = 1)

  lazy val TEST = new Settings(
    nrOfRunways = 1,
    taxiingMaxDuration = 10,
    taxiwayCapacity = 1,
    nrOfGates = 1,
    landingMaxDuration = 300,
    unloadingPassengersMaxDuration = 500,
    outOfKerozenTimeout = 1000,
    ackMaxDuration = 100,
    radioFability = 1)
}

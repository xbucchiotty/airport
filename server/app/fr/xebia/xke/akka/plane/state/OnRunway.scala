package fr.xebia.xke.akka.plane.state

import akka.actor.ActorRef
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, Taxiing}
import fr.xebia.xke.akka.airport.command.{Taxi, Contact}
import fr.xebia.xke.akka.plane.Plane

private[plane] trait OnRunwayWaitingForTaxiway extends Plane with RadioCommunication {

  def waitingToTaxi(airControl: ActorRef, runway: ActorRef) = State("runway", LoggingReceive {

    case Contact(groundControl) =>
      val atc = sender

      replyWithRadio(to = atc)(() => {

        transitionTo(transition = () => {

          groundControl ! Incoming

        })(nextState = incoming(airControl, runway, groundControl))
      })
  })

  def incoming(airControl: ActorRef, runway: ActorRef, groundControl: ActorRef) = State("runway", LoggingReceive {
    case Taxi(taxiway) =>
      val gc = sender

      replyWithRadio(to = gc)(() => {

        transitionTo(transition = () => {

          runway ! HasLeft
          airControl ! HasLeft
          taxiway ! Taxiing

        })(nextState = taxiing(groundControl, taxiway))
      })
  })

  def taxiing(groundControl: ActorRef, taxiway: ActorRef): State

}

private[plane] trait LandingAsLastStep extends Plane with RadioCommunication {

  def waitingToTaxi(airControl: ActorRef, runway: ActorRef) = State("runway", LoggingReceive {
    case Contact(groundControl) =>
      val atc = sender
      replyWithRadio(to = atc)(() => {

        runway ! HasLeft
        airControl ! HasLeft

        done()
      })
  })
}
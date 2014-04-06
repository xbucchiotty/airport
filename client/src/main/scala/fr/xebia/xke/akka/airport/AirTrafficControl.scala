package fr.xebia.xke.akka.airport

import akka.actor.{ActorLogging, ActorRef, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{HasLeft, Incoming, HasLanded}
import fr.xebia.xke.akka.airport.command.{Contact, Land}

class AirTrafficControl extends Actor with ActorLogging {

  def receive = uninitialized

  var groundControl: ActorRef = null

  log.info("ATC created")

  def uninitialized: Receive = {
    case InitAirTrafficControl(groundControlRef, runways, ackMaxTimeout) =>
      sender ! AirTrafficControlReady

      log.info("ATC ready")

      groundControl = groundControlRef

      context become (ready(runways, ackMaxTimeout) orElse uninitialized)
  }

  def ready(runways: Seq[ActorRef], ackMaxTimeout: Int): Receive = {
    //Plane incomes from the sky
    case Incoming =>
      val plane = sender
      //it requests to land
      //you should tell the sender (the plane)
      //to land on a free runway
      plane ! Land(runways.head)

    //and stores in this actor
    //that the targeted runway is allocated to this plane

    //if there is no runway available
    //you should not reply now
    //but stashing the request
    //to call him back when a runway will be free

    //A plane has landed
    case HasLanded =>
      val plane = sender
      //It does not know yet the ground control
      //You reply with the reference to the ground control
      //Nothing very usefull to add there
      plane ! Contact(groundControl)

    //The plane has left the runway
    case HasLeft =>
      val plane = sender
    //It's now free to accept a new plane
    //and if the actor has stashed request
    //it's time to reply to  them

  }

}
package fr.xebia.xke.akka.airport

import akka.actor.{ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{EndOfTaxi, HasParked, HasLeft, Incoming}
import fr.xebia.xke.akka.airport.command.{ParkAt, Taxi}

class GroundControl extends Actor with ActorLogging {

  def receive = uninitialized

  log.info("GroundCountrol created")

  def uninitialized: Receive = {
    case InitGroundControl(taxiways: Seq[ActorRef], gates: Seq[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int) =>
      sender ! GroundControlReady
      context become (ready(taxiways, gates, taxiwayCapacity, ackMaxDuration) orElse uninitialized)
      log.info("GroundCountrol ready")
  }

  def ready(taxiways: Seq[ActorRef], gates: Seq[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int): Receive = {
    //A new plane is on a taxiway and requests to taxi
    case Incoming =>
      val plane = sender
      //we should find him a free taxiway
      //tell him to taxy on it
      sender ! Taxi(taxiways.head)

    //and stores in this actor
    //that the targeted taxiway has on free slot less than before

    //If all the taxiway are full
    //we should not answer yet
    //and stashing its request to taxi
    //and reply when a free slot on a taxiway is gained

    //A plane is at the end of a taxiway
    //It requests a gate
    case EndOfTaxi =>
      val plane = sender
      //We should find him a free gate
      //tell the plane to park
      plane ! ParkAt(gates.head)

    //and stores in this actor
    //that the targeted gate is occupied

    //if all gates are occupied
    //we should not answer yet
    //and stashing its request to park

    //When a gate is free once again
    //we should unstash the request

    //A plane has parked
    case HasParked =>
      val plane = sender

    //The plane is no longer on the taxiway
    //The taxiway has gained one slot free

    //If there is stashed requests to taxi
    //We should unstash one

    //A plane has left a gate
    case HasLeft =>
      val plane = sender
    //The plane has ended the game
    //The gate is now free

    //If there is stashed requests to park
    //We should unstash one


  }

}

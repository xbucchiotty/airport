package fr.xebia.xke.akka.game

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import fr.xebia.xke.akka.airport.PlaneEvent.{EndOfTaxi, HasParked, HasLeft, Incoming}
import fr.xebia.xke.akka.airport.command.{ParkAt, Taxi}
import scala.collection.immutable.Queue
import scala.concurrent.duration._

class GroundControl(taxiways: Set[ActorRef], gates: Set[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int) extends Actor with ActorLogging {

  var pendingsForTaxiways = Queue.empty[ActorRef]

  var taxiwaysAllocations = Map.empty[ActorRef, ActorRef]

  var pendingsForGates = Queue.empty[ActorRef]

  var gatesAllocations = Map.empty[ActorRef, ActorRef]

  def receive: Receive = {
    case Incoming =>
      val plane = sender()

      val freeTaxiways = taxiways -- taxiwaysAllocations.groupBy(_._2).filter(fullTaxiway).keys

      if (freeTaxiways.nonEmpty) {
        val allocatedTaxiway = freeTaxiways.head
        plane ensure Taxi(allocatedTaxiway)

        taxiwaysAllocations += (plane -> allocatedTaxiway)

      } else {

        if (!pendingsForTaxiways.contains(plane)) {

          pendingsForTaxiways = pendingsForTaxiways enqueue plane
        }

      }

    case EndOfTaxi =>
      val plane = sender()

      val freeGates = gates -- gatesAllocations.values

      if (freeGates.nonEmpty) {

        val allocatedGate = freeGates.head

        plane ensure ParkAt(allocatedGate)

        gatesAllocations += (plane -> allocatedGate)

      } else {
        if (!pendingsForGates.contains(plane)) {

          pendingsForGates = pendingsForGates enqueue plane
        }
      }


    case HasParked =>
      val plane = sender()

      val newFreeTaxiway = taxiwaysAllocations(plane)

      taxiwaysAllocations -= plane

      if (pendingsForTaxiways.nonEmpty) {
        val (pendingPlane, newQueue) = pendingsForTaxiways.dequeue

        pendingsForTaxiways = newQueue

        pendingPlane ensure Taxi(newFreeTaxiway)

        taxiwaysAllocations += (pendingPlane -> newFreeTaxiway)
      }


    case HasLeft =>
      val plane = sender()

      val freeGate = gatesAllocations(plane)

      gatesAllocations -= plane

      if (pendingsForGates.nonEmpty) {
        val (pendingPlane, newQueue) = pendingsForGates.dequeue

        pendingsForGates = newQueue

        pendingPlane ensure ParkAt(freeGate)

        gatesAllocations += (pendingPlane -> freeGate)
      }
  }

  implicit class OrderSenderWrapper(plane: ActorRef) {

    def ensure(message: Any) {
      context.actorOf(OrderSender.props(plane, message, ackMaxDuration.milliseconds))
    }

  }

  def fullTaxiway = (entry: (ActorRef, Map[ActorRef, ActorRef])) => entry._2.size == taxiwayCapacity

}

object GroundControl {

  def props(taxiways: Set[ActorRef], gates: Set[ActorRef], taxiwayCapacity: Int, ackMaxDuration: Int) =
    Props(classOf[GroundControl], taxiways, gates, taxiwayCapacity, ackMaxDuration)
}

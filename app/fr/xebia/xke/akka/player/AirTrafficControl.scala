package fr.xebia.xke.akka.player

import akka.actor._
import akka.persistence.EventsourcedProcessor
import fr.xebia.xke.akka.airport.message.{AirTrafficControlReady, InitAirTrafficControl, ChaosMonkey}
import fr.xebia.xke.akka.airport.message.PlaneEvent.{HasLeft, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.message.command.{Contact, Land}

import scala.collection.immutable.Queue

class AirTrafficControl extends EventsourcedProcessor with ActorLogging {

  var groundControl: ActorRef = null
  var ackMaxTimeout: Int = _
  var runways = Set.empty[ActorRef]
  
  var allocations = Map.empty[ActorRef,ActorRef]
  var pendings = Queue.empty[ActorRef]
  
  def freeRunways = runways -- allocations.values

  override def receiveCommand: Receive = {

    case Incoming =>
      val plane = sender()
      
      if(freeRunways.nonEmpty){
          val freeRunway = freeRunways.head
          
          persist(PlaneAffected(plane,freeRunway)){
            case PlaneAffected(p,f) =>
                allocations += (p -> f)
                
                val message = Land(f)
          
                import scala.concurrent.duration._
                context.actorOf(Props(new OrderSender(plane, message, ackMaxTimeout.milliseconds)))
          }

      }else{
          persist(PlaneEnqueued(plane)){
            case PlaneEnqueued(p) =>
                pendings = pendings enqueue plane
          }
      }
      

    case HasLanded =>
      val plane = sender()
      
      val message = Contact(groundControl)
          
      import scala.concurrent.duration._
      context.actorOf(Props(new OrderSender(plane, message, ackMaxTimeout.milliseconds)))
      

    case HasLeft =>
      val plane = sender()
      val freeRunway = allocations(plane)

      persist(PlaneDisaffected(plane)) {
        case PlaneDisaffected(p) =>
            allocations -= p
      }

      if (pendings.nonEmpty) {
        val (pendingPlane, _) = pendings.dequeue

        persist(PlaneDequeued) {
            case _ =>
                val (_, newPendings) = pendings.dequeue
                pendings = newPendings
        }

        persist(PlaneAffected(pendingPlane, freeRunway)) {
            case PlaneAffected(pp, f) =>
                val message = Land(f)
          
                import scala.concurrent.duration._
                context.actorOf(Props(new OrderSender(pp, message, ackMaxTimeout.milliseconds)))
                allocations += (pp -> f)
        }
    }
     


    case ChaosMonkey =>
      log.error("Oh no, a chaos monkey!!!!")
      throw new ChaosMonkeyException

    //Initialization
    case InitAirTrafficControl(_groundControl, _runways, _ackMaxTimeout) =>

      val game = sender()
      
      persist(Initiated(_groundControl, _runways, _ackMaxTimeout)){
        case Initiated(g,r,a) =>{
            this.groundControl = g
            this.runways = r
            this.ackMaxTimeout = a

            game ! AirTrafficControlReady
    }
  }
  }
  
  override def receiveRecover: Receive = {
    case PlaneAffected(plane, runway) =>
        allocations += (plane -> runway)

    case PlaneDisaffected(plane) =>
        allocations -= plane

    case PlaneEnqueued(plane) =>
        pendings = pendings enqueue plane

    case PlaneDequeued =>
        val (_, newPendings) = pendings.dequeue
        pendings = newPendings

    case Initiated(g,r,a) =>
        this.groundControl = g
        this.runways = r
        this.ackMaxTimeout = a
  }

}

case class PlaneAffected(plane:ActorRef,runway:ActorRef)
case class PlaneDisaffected(plane:ActorRef)
case class PlaneEnqueued(plane:ActorRef)
case object PlaneDequeued
case class Initiated(groundControl: ActorRef, runways: Set[ActorRef], ackMaxTimeout:Int)
package fr.xebia.xke.akka.player

import akka.actor._
import akka.persistence.EventsourcedProcessor
import fr.xebia.xke.akka.airport.message.{AirTrafficControlReady, InitAirTrafficControl, ChaosMonkey}
import fr.xebia.xke.akka.airport.message.PlaneEvent.{HasLeft, HasLanded, Incoming}
import fr.xebia.xke.akka.airport.message.command.{Contact, Land}

import scala.collection.immutable.Queue

class AirTrafficControl extends Actor with ActorLogging {

  var groundControl: ActorRef = null
  var ackMaxTimeout: Int = _
  var runways = Set.empty[ActorRef]
  
  var allocations = Map.empty[ActorRef,ActorRef]
  var pendings = Queue.empty[ActorRef]
  
  def freeRunways = runways -- allocations.values

  def receive: Receive = {

    case Incoming =>
      val plane = sender()
      
      if(freeRunways.nonEmpty){
          val freeRunway = freeRunways.head
          
          val message = Land(freeRunway)
          
          import scala.concurrent.duration._
          context.actorOf(Props(new OrderSender(plane, message, ackMaxTimeout.milliseconds)))
          
          allocations += (plane -> freeRunway)
      }else{
          pendings = pendings enqueue plane
      }
      

    case HasLanded =>
      val plane = sender()
      
      val message = Contact(groundControl)
          
      import scala.concurrent.duration._
      context.actorOf(Props(new OrderSender(plane, message, ackMaxTimeout.milliseconds)))
      

    case HasLeft =>
      val plane = sender()
      val freeRunway = allocations(plane)
      allocations -= plane
      
      if(pendings.nonEmpty){
          val (pendingPlane,newPendings) = pendings.dequeue
          
          val message = Land(freeRunway)           

         import scala.concurrent.duration._
         context.actorOf(Props(new OrderSender(pendingPlane, message, ackMaxTimeout.milliseconds)))


          allocations += (pendingPlane -> freeRunway)
          pendings = newPendings
      }


    case ChaosMonkey =>
      log.error("Oh no, a chaos monkey!!!!")
      throw new ChaosMonkeyException

    //Initialization
    case InitAirTrafficControl(_groundControl, _runways, _ackMaxTimeout) =>

      this.groundControl = _groundControl
      this.runways = _runways
      this.ackMaxTimeout = _ackMaxTimeout

      sender() ! AirTrafficControlReady
  }

}

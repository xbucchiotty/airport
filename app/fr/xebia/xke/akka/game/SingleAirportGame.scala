package fr.xebia.xke.akka.game

import akka.actor.OneForOneStrategy
import akka.actor.Terminated
import akka.actor._
import akka.event.EventStream
import concurrent.duration._
import fr.xebia.xke.akka.airport._
import fr.xebia.xke.akka.airport.message.command.Contact
import fr.xebia.xke.akka.game.SinglePlayerGame.NewPlane
import fr.xebia.xke.akka.infrastructure.SessionId
import fr.xebia.xke.akka.infrastructure.cluster.AirportLocator
import fr.xebia.xke.akka.plane.Plane
import fr.xebia.xke.akka.plane.PlaneListener
import fr.xebia.xke.akka.plane.event.PlaneStatus
import languageFeature.postfixOps
import scala.util.Random
import fr.xebia.xke.akka.airport.message.{ChaosMonkey, AirTrafficControlReady, InitAirTrafficControl}

case class SinglePlayerGame(
                             sessionId: SessionId,
                             settings: Settings,
                             planeType: Class[_ <: Plane],
                             gameEventStream: EventStream,
                             airport: Airport,
                             airportClusterLocation: ActorRef) extends Actor with ActorLogging {

  import settings._

  val runways: Set[ActorRef] =
    for (i <- (1 to nrOfRunways).toSet[Int])
    yield context.actorOf(Runway.props(), s"runway-$i")

  val taxiways: Set[ActorRef] =
    for (i <- (1 to nrOfTaxiways).toSet[Int])
    yield context.actorOf(Taxiway.props(settings), s"taxiway-$i")

  val gates: Set[ActorRef] =
    for (i <- (1 to nrOfGates).toSet[Int])
    yield context.actorOf(Gate.props(), s"gate-$i")

  var planeGeneration: Cancellable = null

  var score = 0

  var planesToGenerate: Int = totalPlanes

  def totalPlanes: Int = settings.objective / 2

  var isAirportConnected = true

  override def preStart() {
    runways.foreach(context.watch)
    taxiways.foreach(context.watch)
    gates.foreach(context.watch)

    gameEventStream.subscribe(self, classOf[PlaneStatus])

    publishScore()
  }

  def receive = idle

  def idle: Receive = {
    case SinglePlayerGame.InitGame(airTrafficControl) =>

      val groundControl = context.actorOf(GroundControl.props(taxiways, gates, taxiwayCapacity, settings.ackMaxDuration), "groundControl")

      airTrafficControl ! InitAirTrafficControl(groundControl, runways, settings.ackMaxDuration)

      context become waitingForTheGameReady(airTrafficControl, groundControl)

  }

  def waitingForTheGameReady(airTrafficControl: ActorRef, groundControl: ActorRef): Receive = {
    case AirTrafficControlReady =>
      import context.dispatcher
      planeGeneration = context.system.scheduler.schedule(1 second, planeGenerationInterval milliseconds, self, NewPlane)

      context become started(airTrafficControl, groundControl)

  }

  def started(airTrafficControl: ActorRef, groundControl: ActorRef): Receive = {
    case PlaneStatus("done", _, _, _) =>
      gain()

    case PlaneStatus(_, _, _, error) if error.nonEmpty =>
      loose()

    case PlayerUp(_) =>
      isAirportConnected = true

    case PlayerDown(_) =>
      isAirportConnected = false

    case Terminated(_) =>
      gameEventStream.publish(GameOver)
      context stop self

    case NewPlane if planesToGenerate > 0 =>

      val anArrivalFlight = airport.arrivals(Random.nextInt(airport.arrivals.size))

      val planeEventStream = new EventStream()

      val plane = context.actorOf(Props(planeType, settings, planeEventStream), s"${anArrivalFlight.airline}-${Random.nextLong() % 100000}")
      val listener = context.actorOf(PlaneListener.props(plane, gameEventStream))

      planeEventStream.subscribe(listener, classOf[Any])

      plane.tell(Contact(airTrafficControl), airTrafficControl)

      planesToGenerate -= 1


      if (settings.chaosMonkey && planesToGenerate == (totalPlanes / 2)) {
        log.warning("Oh no, a chaos monkey is coming and shut you down!")
        airTrafficControl ! ChaosMonkey
      }
  }

  private def publishScore() {
    gameEventStream.publish(Score(score, objective))
  }

  private def gain() {
    score = Math.min(score + 2, objective)
    publishScore()

    if (score == objective) {
      gameEventStream.publish(GameEnd)
      planeGeneration.cancel()

      context stop self
    }
  }

  private def loose() {
    score = Math.max(score - 3, 0)
    publishScore()
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case _ => SupervisorStrategy.Stop
  }

}

object SinglePlayerGame {

  def props(
             sessionId: SessionId,
             settings: Settings,
             planeType: Class[_ <: Plane],
             gameEventStream: EventStream,
             airport: Airport,
             airportClusterLocation: ActorRef): Props =
    Props(new SinglePlayerGame(sessionId, settings, planeType, gameEventStream, airport, airportClusterLocation))

  case object NewPlane

  case class ErrorInGame(cause: String) extends Exception(cause)

  case class InitGame(airTrafficControl: ActorRef)

  case class GameInitialized(airTrafficControl: ActorRef, groundControl: ActorRef) extends GameEvent

}
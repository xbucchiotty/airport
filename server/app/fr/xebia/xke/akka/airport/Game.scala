package fr.xebia.xke.akka.airport

import akka.actor.{OneForOneStrategy, SupervisorStrategy, ActorRef, Cancellable, Terminated, ActorLogging, Props, Actor}
import concurrent.duration._
import controllers.PlaneStatus
import fr.xebia.xke.akka.airport.Game.NewPlane
import languageFeature.postfixOps
import scala.util.Random
import akka.event.EventStream

class Game(settings: Settings, planeType: Class[Plane], eventStream: EventStream) extends Actor with ActorLogging {

  import settings._

  val runways: Seq[ActorRef] =
    for (i <- 1 to nrOfRunways)
    yield context.actorOf(Props[Runway], s"runway-$i")

  val taxiways: Seq[ActorRef] =
    for (i <- 1 to nrOfTaxiways)
    yield context.actorOf(Props(classOf[Taxiway], settings), s"taxiway-$i")

  val gates: Seq[ActorRef] =
    for (i <- 1 to nrOfGates)
    yield context.actorOf(Props[Gate], s"gate-$i")

  var planeGeneration: Cancellable = null

  var score = 0

  var planesToGenerate: Int = settings.objective / 2

  override def preStart() {
    runways.foreach(context.watch)
    taxiways.foreach(context.watch)
    gates.foreach(context.watch)

    eventStream.subscribe(self, classOf[PlaneStatus])

    publishScore()
  }

  def receive = idle

  def idle: Receive = {
    case GameStart(airTrafficControlLookup, groundControlLookup) =>

      airTrafficControlLookup ! InitAirTrafficControl(runways, settings.ackMaxDuration)
      groundControlLookup ! InitGroundControl(taxiways, gates, taxiwayCapacity, settings.ackMaxDuration)

      context become waitingForTheGameReady(null, null)
  }

  def waitingForTheGameReady(airTrafficControl: ActorRef, groundControl: ActorRef): Receive = {
    case AirTrafficControlReady =>
      val airTrafficControl = sender
      context watch airTrafficControl

      if (groundControl != null) {
        import context.dispatcher
        planeGeneration = context.system.scheduler.schedule(1 second, planeGenerationInterval milliseconds, self, NewPlane)

        context become started(airTrafficControl, groundControl)
      } else {
        context become waitingForTheGameReady(airTrafficControl, null)
      }

    case GroundControlReady =>
      val groundControl = sender
      context watch groundControl

      if (airTrafficControl != null) {
        import context.dispatcher
        planeGeneration = context.system.scheduler.schedule(1 second, planeGenerationInterval milliseconds, self, NewPlane)

        context become started(airTrafficControl, groundControl)
      } else {
        context become waitingForTheGameReady(null, groundControl)
      }
  }

  def started(airTrafficControl: ActorRef, groundControl: ActorRef): Receive = {
    case PlaneStatus("done", _, _, _) =>
      gain()

    case PlaneStatus(_, _, _, error) if error.nonEmpty =>
      loose()

    case Terminated(_) =>
      eventStream.publish(GameOver)
      context stop self

    case NewPlane if planesToGenerate > 0 =>
      context.actorOf(Props(planeType, airTrafficControl, self, settings, eventStream), s"AF-${ Random.nextLong() % 100000 }")
      planesToGenerate -= 1

  }

  private def publishScore() {
    eventStream.publish(Score(score, objective))
  }

  private def gain() {
    score = Math.min(score + 2, objective)
    publishScore()

    if (score == objective) {
      eventStream.publish(GameEnd)
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

object Game {

  case object NewPlane

  case class ErrorInGame(cause: String) extends Exception(cause)

}
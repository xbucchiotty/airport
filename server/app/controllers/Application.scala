package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport._
import play.api.libs.iteratee.Iteratee
import play.api.mvc._
import play.api.templates.HtmlFormat
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.ClusterDomainEvent
import fr.xebia.xke.akka.airport.JustLandingPlane
import fr.xebia.xke.akka.airport.GameStart
import fr.xebia.xke.akka.airport.JustTaxiingPlane
import fr.xebia.xke.akka.airport.FullStepPlane

object Application extends Controller {

  def index = Action {
    Redirect(routes.Application.level0)
  }

  def level0 = Action {
    val settings = Settings(
      nrOfRunways = 1,
      landingMaxDuration = 1500,
      planeGenerationInterval = 3000,
      objective = 20,
      ackMaxDuration = 500)

    newGame(settings, views.html.level_0(settings), classOf[JustLandingPlane])
  }

  def level1 = Action {
    val settings = Settings(
      nrOfRunways = 2,
      landingMaxDuration = 1500,
      planeGenerationInterval = 1250,
      objective = 20,
      ackMaxDuration = 500)

    newGame(settings, views.html.level_1(settings), classOf[JustLandingPlane])
  }

  def level2 = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      ackMaxDuration = 100,
      outOfKerozenTimeout = 30000)

    newGame(settings, views.html.level_2(settings), classOf[JustLandingPlane])
  }

  def level3 = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      nrOfTaxiways = 1,
      taxiingDuration = 1000,
      taxiwayCapacity = 5,
      ackMaxDuration = 100,
      outOfKerozenTimeout = 30000)

    newGame(settings, views.html.level_3(settings), classOf[JustTaxiingPlane])
  }

  def level4 = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      nrOfTaxiways = 2,
      taxiingDuration = 1000,
      taxiwayCapacity = 10,
      nrOfGates = 2,
      unloadingPassengersMaxDuration = 5000,
      ackMaxDuration = 100,
      outOfKerozenTimeout = 30000)

    newGame(settings, views.html.level_4(settings), classOf[FullStepPlane])
  }

  def level5 = Action {
    val settings = Settings(
      nrOfRunways = 4,
      landingMaxDuration = 2500,
      planeGenerationInterval = 500,
      objective = 50,
      nrOfTaxiways = 2,
      taxiingDuration = 1000,
      taxiwayCapacity = 10,
      nrOfGates = 2,
      unloadingPassengersMaxDuration = 5000,
      ackMaxDuration = 100,
      radioReliability = 0.8,
      outOfKerozenTimeout = 30000)

    newGame(settings, views.html.level_5(settings), classOf[FullStepPlane])
  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane]) = {

    if (game != null) {
      airportActorSystem.stop(game)
      game = null
    }

    game = airportActorSystem.actorOf(Props(classOf[Game], settings, planeType), s"game-session-$gameCounter")
    gameCounter += 1

    if (listener != null) {
      airportActorSystem.eventStream.unsubscribe(listener)
      airportActorSystem.stop(listener)
    }

    listener = airportActorSystem.actorOf(Props[EventListener])

    airportActorSystem.eventStream.subscribe(listener, classOf[GameEvent])

    if (isPlayerUp) {
      airportActorSystem.eventStream.publish(PlayerUp(playerAddress))
    }

    Ok(template)
  }

  private def startGame() {
    if (isPlayerUp) {

      val airTrafficControl = airportActorSystem.actorSelection(
        ActorPath.fromString(playerAddress.toString) / "user" / "airTrafficControl")

      val groundControl = airportActorSystem.actorSelection(
        ActorPath.fromString(playerAddress.toString) / "user" / "groundControl")

      airportActorSystem.eventStream.subscribe(listener, classOf[PlaneStatus])

      game.tell(GameStart(airTrafficControl, groundControl), Inbox.create(airportActorSystem).getRef())
    }
    else {
      println("Can't start game without player")
    }
  }

  def events = WebSocket.using[String] {
    request =>

    // Log events to the console
      import scala.concurrent.ExecutionContext.Implicits.global
      val in = Iteratee.foreach[String] {
        case "start" =>
          startGame()
      }

      val out = Enumerator2.infiniteUnfold(listener) {
        listener =>
          ask(listener, DequeueEvents)(Timeout(1 second))
            .mapTo[Option[String]]
            .map(replyOption => replyOption.map(reply => (listener, reply))
          )
      }

      (in, out)
  }

  private val airportActorSystem = {
    val system = ActorSystem.create("airportSystem")
    val playerWatcher = system.actorOf(Props[PlayerWatcher], "playerWatcher")
    Cluster(system).subscribe(playerWatcher, classOf[ClusterDomainEvent])
    system
  }

  private var gameCounter = 0

  private var playerAddress: Address = null

  def playerOnline(address: Address) {
    playerAddress = address
    airportActorSystem.eventStream.publish(PlayerUp(address))
  }

  def playerOffline(address: Address) {
    playerAddress = null
    airportActorSystem.eventStream.publish(PlayerDown(address))
  }

  def isPlayerUp =
    playerAddress != null

  private var listener: ActorRef = null
  private var game: ActorRef = null
}

case object DequeueEvents
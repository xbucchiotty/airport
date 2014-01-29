package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport._
import play.api.libs.iteratee.Iteratee
import play.api.mvc._
import play.api.templates.HtmlFormat
import fr.xebia.xke.akka.airport.PlayerDown
import fr.xebia.xke.akka.airport.PlayerUp
import fr.xebia.xke.akka.airport.JustLandingPlane
import fr.xebia.xke.akka.airport.GameStart
import fr.xebia.xke.akka.airport.JustTaxiingPlane
import fr.xebia.xke.akka.airport.FullStepPlane

object Application extends SecuredController with AirportActorSystem {

  def index: Action[AnyContent] =
    LoggedInAction(_ => _ => Redirect(routes.Application.level0))

  def level0 = LoggedInAction {
    _ =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 1,
          landingMaxDuration = 1500,
          planeGenerationInterval = 3000,
          objective = 20,
          ackMaxDuration = 500)

        newGame(settings, views.html.level_0(settings), classOf[JustLandingPlane])

  }

  def level1 = LoggedInAction {
    _ =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 2,
          landingMaxDuration = 1500,
          planeGenerationInterval = 1250,
          objective = 20,
          ackMaxDuration = 500)

        newGame(settings, views.html.level_1(settings), classOf[JustLandingPlane])
  }

  def level2 = LoggedInAction {
    _ =>
      implicit request =>
        val settings = Settings(
          nrOfRunways = 4,
          landingMaxDuration = 2500,
          planeGenerationInterval = 500,
          objective = 50,
          ackMaxDuration = 100,
          outOfKerozenTimeout = 30000)

        newGame(settings, views.html.level_2(settings), classOf[JustLandingPlane])
  }

  def level3 = LoggedInAction {
    _ =>
      implicit request =>
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

  def level4 = LoggedInAction {
    _ =>
      implicit request =>
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

  def level5 = LoggedInAction {
    _ =>
      implicit request =>
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

  def events = WebSocket.using[String] {
    implicit request =>

      val teamMail = currentUser(session).map(_.mail)
      // Log events to the console

      import scala.concurrent.ExecutionContext.Implicits.global
      val in = Iteratee.foreach[String] {
        case "start" => {
          startGame(teamMail)
        }
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

  private def startGame(teamMail: Option[String]) {
    for {
      key <- teamMail
      user <- users.get(key)
      address <- user.playerSystemAddress
    } {
      val airTrafficControl = airportActorSystem.actorSelection(
        ActorPath.fromString(address.toString) / "user" / "airTrafficControl")

      val groundControl = airportActorSystem.actorSelection(
        ActorPath.fromString(address.toString) / "user" / "groundControl")

      airportActorSystem.eventStream.subscribe(listener, classOf[PlaneStatus])

      game.tell(GameStart(airTrafficControl, groundControl), Inbox.create(airportActorSystem).getRef())
    }

  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane])(implicit request: play.api.mvc.Request[_]) = {

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

    for {
      user <- currentUser(session)
      address <- user.playerSystemAddress
    } {
      airportActorSystem.eventStream.publish(PlayerUp(address))
    }

    Ok(template)
  }

  private var gameCounter = 0

  def playerOnline(address: Address) {

    systems += (address.host.get -> address)

    for (user <- findUserBy(address)) {
      users = users.updated(user.mail, user.copy(playerSystemAddress = Some(address)))

      airportActorSystem.eventStream.publish(PlayerUp(address))
    }
  }

  def playerOffline(address: Address) {

    systems -= address.host.get

    for {
      user <- findUserBy(address)
      playerSystemAddress <- user.playerSystemAddress if address == playerSystemAddress
    } {
      users = users.updated(user.mail, user.copy(playerSystemAddress = None))
      airportActorSystem.eventStream.publish(PlayerDown(address))
    }
  }

  private def findUserBy(address: Address): Option[UserInfo] = {
    users.values.find(_.host == address.host.get)
  }

  private var listener: ActorRef = null
  private var game: ActorRef = null
}

case object DequeueEvents
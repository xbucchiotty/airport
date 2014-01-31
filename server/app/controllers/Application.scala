package controllers

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import fr.xebia.xke.akka.airport._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc._
import play.api.templates.HtmlFormat
import fr.xebia.xke.akka.airport.PlayerDown
import fr.xebia.xke.akka.airport.PlayerUp
import fr.xebia.xke.akka.airport.JustLandingPlane
import fr.xebia.xke.akka.airport.GameStart
import fr.xebia.xke.akka.airport.JustTaxiingPlane
import fr.xebia.xke.akka.airport.FullStepPlane
import akka.event.EventStream

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

      val optionalOutput: Option[Enumerator[String]] = for {
        user <- currentUser(session)
        context <- contexts.get(user.mail)
      } yield {
        Enumerator2.infiniteUnfold(context.listener) {
          listener => {
            ask(context.listener, DequeueEvents)(Timeout(1 second))
              .mapTo[Option[String]]
              .map(replyOption => replyOption.map(reply => (listener, reply))
            )
          }
        }
      }

      val out = optionalOutput.getOrElse(Enumerator.empty[String])

      (in, out)
  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane])(implicit request: play.api.mvc.Request[_]) = {
    for (user <- currentUser(session)) {

      for (gameContext <- contexts.get(user.mail)) {
        airportActorSystem.stop(gameContext.game)
        airportActorSystem.stop(gameContext.listener)

        contexts -= user.mail
      }

      val eventStream = new EventStream(false)

      val listener = airportActorSystem.actorOf(Props(classOf[EventListener], eventStream))

      eventStream.subscribe(listener, classOf[GameEvent])
      eventStream.subscribe(listener, classOf[PlaneStatus])


      val game = airportActorSystem.actorOf(Props(classOf[Game], settings, planeType, eventStream), s"game-session-$gameCounter")
      gameCounter += 1


      for (address <- user.playerSystemAddress) {
        println("Event published")
        eventStream publish PlayerUp(address)
      }

      contexts += (user.mail -> GameContext(listener, game, eventStream))
    }

    Ok(template)
  }

  private def startGame(teamMail: Option[TeamMail]) {
    for {
      key <- teamMail
      user <- users.get(key)
      address <- user.playerSystemAddress
      gameContext <- contexts.get(user.mail)
    } {

      val airTrafficControl = airportActorSystem.actorSelection(
        ActorPath.fromString(address.toString) / "user" / "airTrafficControl")

      val groundControl = airportActorSystem.actorSelection(
        ActorPath.fromString(address.toString) / "user" / "groundControl")

      gameContext.game.tell(GameStart(airTrafficControl, groundControl), Inbox.create(airportActorSystem).getRef())
    }

  }

  def playerOnline(address: Address) {

    systems += ((HostName from address) -> address)

    for {
      user <- findUserBy(address)
      gameContext <- contexts.get(user.mail)
    } {
      users = users.updated(user.mail, user.copy(playerSystemAddress = Some(address)))

      gameContext.eventBus.publish(PlayerUp(address))
    }
  }

  def playerOffline(address: Address) {

    systems -= (HostName from address)

    for {
      user <- findUserBy(address)
      playerSystemAddress <- user.playerSystemAddress if address == playerSystemAddress
      gameContext <- contexts.get(user.mail)
    } {
      users = users.updated(user.mail, user.copy(playerSystemAddress = None))
      gameContext.eventBus.publish(PlayerDown(address))
    }
  }

  private def findUserBy(address: Address): Option[UserInfo] = {
    users.values.find(_.host == HostName.from(address))
  }

  private var gameCounter = 0
  private var contexts: Contexts = Contexts.empty
}

case object DequeueEvents
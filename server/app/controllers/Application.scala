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
          println("start")
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

  private def startGame(teamMail: Option[TeamMail]) {
    println(s"teammail " + teamMail)
    println("user: ")
    users.get(teamMail.get).foreach(println)

    println("systemAddress: ")
    users.get(teamMail.get).map(user => user.playerSystemAddress).foreach(println)

    println("gameContext: ")
    users.get(teamMail.get).map(user => contexts.get(user.mail)).foreach(println)

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

      airportActorSystem.eventStream.subscribe(gameContext.listener, classOf[PlaneStatus])

      gameContext.game.tell(GameStart(airTrafficControl, groundControl), Inbox.create(airportActorSystem).getRef())
    }

  }

  private def newGame(settings: Settings, template: HtmlFormat.Appendable, planeType: Class[_ <: Plane])(implicit request: play.api.mvc.Request[_]) = {
    for {
      user <- currentUser(session)
    } {

      println(s"Context before ${contexts.get(user.mail).foreach(println)}")

      for (gameContext <- contexts.get(user.mail)) {
        airportActorSystem.stop(gameContext.game)
        airportActorSystem.eventStream.unsubscribe(gameContext.listener)
        airportActorSystem.stop(gameContext.listener)

        contexts -= user.mail
        println("Removing old game context")

      }

      val newGame = airportActorSystem.actorOf(Props(classOf[Game], settings, planeType), s"game-session-$gameCounter")
      gameCounter += 1

      val newListener = airportActorSystem.actorOf(Props[EventListener])
      airportActorSystem.eventStream.subscribe(newListener, classOf[GameEvent])

      for (address <- user.playerSystemAddress) {
        airportActorSystem.eventStream.publish(PlayerUp(address))
      }

      contexts += (user.mail -> GameContext(newListener, newGame))
      println("Creating new context")

      println(s"Context after ${contexts.get(user.mail)}")
    }

    Ok(template)
  }

  def playerOnline(address: Address) {

    systems += ((HostName from address) -> address)

    for (user <- findUserBy(address)) {
      users = users.updated(user.mail, user.copy(playerSystemAddress = Some(address)))

      airportActorSystem.eventStream.publish(PlayerUp(address))
    }
  }

  def playerOffline(address: Address) {

    systems -= (HostName from address)

    for {
      user <- findUserBy(address)
      playerSystemAddress <- user.playerSystemAddress if address == playerSystemAddress
    } {
      users = users.updated(user.mail, user.copy(playerSystemAddress = None))
      airportActorSystem.eventStream.publish(PlayerDown(address))
    }
  }

  private def findUserBy(address: Address): Option[UserInfo] = {
    users.values.find(_.host == HostName.from(address))
  }

  private var gameCounter = 0
  private var contexts: Contexts = Contexts.empty
}

case object DequeueEvents
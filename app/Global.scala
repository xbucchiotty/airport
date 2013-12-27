import akka.actor.Props
import fr.xebia.xke.akka.airport.{Game, GameConfiguration}
import play.api.libs.concurrent.Akka
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    import play.api.Play.current
    Akka.system.actorOf(Props(classOf[Game], GameConfiguration()), name = "game")
  }
}
import akka.actor.{Props, ActorSystem}
import fr.xebia.xke.akka.airport.{GameConfiguration, Game}

object Launcher extends App {

  val system = ActorSystem()

  system.actorOf(
    Props(classOf[Game], GameConfiguration()), "game")


}

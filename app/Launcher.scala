import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import fr.xebia.xke.akka.airport.command.Command

object Launcher {

  def main(args: Array[String]) {
    val config = ConfigFactory.load()
    val system = ActorSystem.create("playerSystem", config.getConfig("player"))



    system.awaitTermination()
  }



}

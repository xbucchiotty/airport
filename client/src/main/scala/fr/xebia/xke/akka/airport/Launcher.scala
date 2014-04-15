import akka.actor.ActorSystem

import com.typesafe.config.ConfigFactory
import fr.xebia.xke.akka.airport.AirportManager
import scala.annotation.tailrec
import java.util.List
import collection.JavaConversions._

object Launcher {
  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val airportCode = conf.getStringList("akka.cluster.roles").headOption.getOrElse("")
    if(airportCode.isEmpty){
      println()
      println()
      println(s"You must provide an airport code in build.sbt before you can start the airport")
      println()
      println()
    }else {
      val system = ActorSystem.create("airportSystem")
      system.actorOf(AirportManager.props, airportCode)

      println()
      println()
      println(s"Airport $airportCode started, press CTRL+D to stop")
      println()
      println()

      if (shouldTerminate) {
        println("Shutting down...")
        system.shutdown()
        system.awaitTermination()
      }
    }
  }

  def isEOF(c: Int): Boolean = c == -1

  @tailrec def shouldTerminate: Boolean = System.in.available > 0 && isEOF(System.in.read()) || shouldTerminate
}

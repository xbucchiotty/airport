import akka.actor.{Props, ActorSystem}

import fr.xebia.xke.akka.airport.Airport
import scala.annotation.tailrec

object Launcher {
  def main(args: Array[String]) {
    val system = ActorSystem.create("airportSystem")

    system.actorOf(Props[Airport], "airport")
    
    println()
    println()
    println("Airport started, press CTRL+D to stop")
    println()
    println()

    if(shouldTerminate){
      println("Shutting down...")
      system.shutdown()
      system.awaitTermination()
    }
  }

  def isEOF(c: Int): Boolean = c == -1
  @tailrec def shouldTerminate: Boolean = System.in.available > 0 && isEOF(System.in.read()) || shouldTerminate
}

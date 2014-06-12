import sbt._
import Keys._

object AirportBuild extends Build {

  lazy val xke_airport = (project in file(".")).enablePlugins(play.PlayScala)

  lazy val seedNode = settingKey[String]("URL endpoint to the master of the game")

  lazy val airport = settingKey[String]("Your airport inside the cluster")

  lazy val check = taskKey[Unit]("Check your config")

}

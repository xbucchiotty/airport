import sbt._
import Keys._

object AirportBuild extends Build {


  lazy val root = Project(id = "airport", base = file(".")).aggregate(messages, client).settings(  addCommandAlias("start", ";project client; run"):_*  )

  lazy val messages = Project(id = "messages", base = file("messages"))

  lazy val client = Project(id = "client", base = file("client")).dependsOn(messages)

  lazy val seedNode = settingKey[String]("URL endpoint to the master of the game")

  lazy val airport = settingKey[String]("Your airport inside the cluster")

  lazy val check = taskKey[Unit]("Check your config")

}
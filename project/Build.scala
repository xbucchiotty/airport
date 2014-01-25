import sbt._
import Keys._

object AirportBuild extends Build {

  lazy val root = Project(id = "airport",
    base = file(".")) aggregate(messages, client)

  lazy val messages = Project(id = "messages",
    base = file("messages"))

  lazy val client = Project(id = "client",
    base = file("client")) dependsOn(messages)

}
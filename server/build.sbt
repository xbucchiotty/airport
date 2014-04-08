version := "1.0.0"

organization := "fr.xebia.xke.akka.airport"

name := "server"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.1" % "test",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.1"
)

javaOptions in run += "-Xmx256M -server"

play.Project.playScalaSettings

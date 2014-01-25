version := "1.0.0"

organization := "fr.xebia.xke.akka.airport"

name := "server"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3" % "test",
  "com.typesafe.akka" %% "akka-cluster" % "2.2.3"
)

play.Project.playScalaSettings

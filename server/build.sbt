version := "1.0.0"

organization := "fr.xebia.xke.akka.airport"

name := "server"

scalaVersion := "2.11.1"


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.3"
)

javaOptions in run += "-Xmx256M -server"

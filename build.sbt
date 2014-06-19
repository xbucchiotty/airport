version := "1.0.0"

organization := "fr.xebia.xke.akka.airport"

scalaVersion := "2.11.1"


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.3",
  "ch.qos.logback" % "logback-classic" % "1.1.1",
  "ch.qos.logback" % "logback-core" % "1.1.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test",
  "com.typesafe.akka" %% "akka-cluster" % "2.3.3",
  "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.3")

incOptions := incOptions.value.withNameHashing(true)

mainClass in (Compile,run) := Some("fr.xebia.xke.akka.player.Launcher")

version := "1.0.0"

organization := "fr.xebia.xke.akka.airport"

scalaVersion := "2.11.7"

lazy val xke_airport = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.akka" %% "akka-cluster" % "2.5.4",
  "com.typesafe.akka" %% "akka-persistence" % "2.5.4",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % "test"
)

incOptions := incOptions.value.withNameHashing(true)

fork in run := true

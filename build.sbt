name := "sangria-relay-playground"
description := "An example of GraphQL server supporting relay written with Play and sangria."

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.6"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  filters,
  guice,
  "org.sangria-graphql" %% "sangria-relay" % "2.1.0",
  "org.sangria-graphql" %% "sangria-play-json" % "2.0.1"
)

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala)

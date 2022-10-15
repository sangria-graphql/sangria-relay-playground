name := "sangria-relay-playground"
description := "An example of GraphQL server supporting relay written with Play and sangria."

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.8"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  filters,
  guice,
  "org.sangria-graphql" %% "sangria-relay" % "2.1.1",
  "org.sangria-graphql" %% "sangria-play-json" % "2.0.2"
)

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "sangria-relay-playground"
description := "An example of GraphQL server supporting relay written with Play and sangria."

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.6"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  filters,
  guice,
  "org.sangria-graphql" %% "sangria-relay" % "2.0.0",
  "org.sangria-graphql" %% "sangria-play-json" % "2.0.1"
)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala)

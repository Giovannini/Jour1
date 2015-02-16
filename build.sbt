name := "projet"

version := "0.1-SNAPSHOT"

lazy val graph = (project in file("modules/graph"))
  .enablePlugins(PlayScala)

lazy val rules = (project in file("modules/rules"))
  .enablePlugins(PlayScala)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(graph, rules)
  .aggregate(graph, rules)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc, anorm , cache, ws)

libraryDependencies += "org.scalatestplus" %% "play" % "1.2.0" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

scalacOptions += "-feature"
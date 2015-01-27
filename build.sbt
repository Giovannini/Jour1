name := "projet"

version := "0.1-SNAPSHOT"

lazy val graph = (project in file("modules/graph"))
  .enablePlugins(PlayScala)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(graph)
  .aggregate(graph)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws)

libraryDependencies += "org.scalatestplus" %% "play" % "1.2.0" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "2.13"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

name := "projet"

version := "0.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.5"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq( jdbc, anorm , cache, ws,
  "org.anormcypher" %% "anormcypher" % "0.6.0",
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

scalacOptions ++= Seq("-feature", "-language:reflectiveCalls")
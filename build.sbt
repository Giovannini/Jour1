name := "projet"

version := "0.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq( jdbc, anorm , cache, ws,
  "org.anormcypher" %% "anormcypher" % "0.6.0",
  "mysql" % "mysql-connector-java" % "5.1.27",
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "org.scala-lang.modules" %% "scala-async" % "0.9.2"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

scalacOptions ++= Seq( "-feature", "-language:reflectiveCalls",
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions"/*,
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import",     // 2.11 only
  "-Yno-predef" */  // no automatic import of Predef (removes irritating implicits)
)

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

scalacOptions ++= Seq(
  "-feature",                 //safer Scala
  "-deprecation",             //safer Scala
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-language:reflectiveCalls",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",               //safer Scala
  "-Xfatal-warnings",         //safer Scala
  "-Xlint"/*,                 //safer Scala
  "-Yno-adapted-args",    //Do not adapt an argument list to match the receiver
  "-Ywarn-dead-code",        //Warn when dead code is identified
  "-Ywarn-unused",        //Warn when local and private vals, vars, defs and types are unused
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard", //Warn when non-Unit result expressions are unused
  "-Xfuture",
  "-Ywarn-unused-import",     // 2.11 only
  "-Yno-import",     // Compile without importing scala.*, java.lang.* or Predef
  "-Yno-predef" */  // no automatic import of Predef (removes irritating implicits)
)

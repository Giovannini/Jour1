name := "rules"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq( jdbc , anorm , cache , ws)

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.25"
)
name := "graph"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "anormcypher" at "http://repo.anormcypher.org/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "org.anormcypher" %% "anormcypher" % "0.6.0"
)

libraryDependencies ++= Seq( jdbc , anorm , cache , ws)




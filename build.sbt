name := "QueryCircleAPI"

version := "1.0"

scalaVersion := "2.11.6"

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.3.7",
  "com.typesafe.play" %% "play-json" % "2.3.7"
)
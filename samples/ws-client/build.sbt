name := "ws-client"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.ning" % "async-http-client" % "1.7.17",
  "todo" %% "todo" % "1.0-SNAPSHOT"
)

resolvers += "Local M2" at Path.userHome.asURL + ".m2/repository"

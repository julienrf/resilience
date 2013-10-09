name := "todo"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0-SNAPSHOT",
  cache
)

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

scalacOptions ++= Seq("-Xlint", "-feature")

play.Project.playScalaSettings
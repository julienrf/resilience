name := "notes"

version := "1.0-SNAPSHOT"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "fr.irisa.resilience" %% "sync" % "0.1-SNAPSHOT",
  "fr.irisa.resilience" %% "mongo-log" % "0.1-SNAPSHOT",
  cache
)

scalacOptions ++= Seq("-Xlint", "-feature")

play.Project.playScalaSettings
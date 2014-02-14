val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  organization := "fr.irisa.resilience",
  scalaVersion := "2.10.3",
  scalacOptions ++= Seq("-Xlint", "-feature")
)

lazy val resilience = project in file(".") aggregate (library, samples)

lazy val library = project in file("src") aggregate (sync, `mongo-log`)

lazy val sync = project in file("src/server/sync") settings (commonSettings: _*) settings (
  libraryDependencies += "com.typesafe.play" %% "play" % "2.2.1"
)

lazy val `mongo-log` = project in file("src/server/mongo-log") settings (commonSettings: _*) settings (
  name := "mongo-log",
  libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"
) dependsOn sync

lazy val samples = project aggregate (`sample-todo`, `sample-notes`)

lazy val `sample-todo` = project in file("samples/todo") settings (commonSettings: _*) settings (
  resolvers += "julienrf.github.com" at "http://julienrf.github.com/repo-snapshots/",
  libraryDependencies += "com.github.julienrf" %% "play-json-variants" % "0.1-SNAPSHOT",
  libraryDependencies += cache
) settings (play.Project.playScalaSettings: _*) dependsOn (sync, `mongo-log`)

lazy val `sample-notes` = project in file("samples/notes") settings (commonSettings: _*) settings (
  libraryDependencies += cache  
) settings (play.Project.playScalaSettings: _*) dependsOn (sync, `mongo-log`)

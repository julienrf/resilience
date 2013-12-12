lazy val todo = project.in(file(".")).settings(
  name := "todo",
  version := "1.0-SNAPSHOT",
  resolvers ++= Seq(
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "julienrf.github.com" at "http://julienrf.github.com/repo-snapshots/"
  ),
  libraryDependencies ++= Seq(
    "fr.irisa.resilience" %% "sync" % "0.1-SNAPSHOT",
    "fr.irisa.resilience" %% "mongo-log" % "0.1-SNAPSHOT",
    "com.github.julienrf" %% "play-json-variants" % "0.1-SNAPSHOT",
    cache
  ),
  scalacOptions ++= Seq("-Xlint", "-feature")
).settings(play.Project.playScalaSettings: _*)

//lazy val jsScala = project.in(file("js-scala")).settings(
//  name := "js-scala",
//  libraryDependencies += "EPFL" %% "js-scala" % "0.4-SNAPSHOT",
//  scalaVersion := "2.10.2-RC1",
//  scalaOrganization := "org.scala-lang.virtualized",
//  scalacOptions ++= Seq("-Yvirtualize")
//)
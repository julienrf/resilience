import sbt._
import Keys._

object Build extends sbt.Build {

  val defaultSettings = Project.defaultSettings ++ Seq(
    scalaVersion := "2.10.3",
    organization := "fr.irisa.resilience",
    version := "0.1-SNAPSHOT"
  )

  lazy val sync = project settings (defaultSettings: _*) settings (
    name := "sync",
    resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies += "com.typesafe.play" %% "play" % "2.2.1"
  )
  
  lazy val mongoLog = project in file("mongo-log") settings (defaultSettings: _*) settings (
    name := "mongo-log",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0-SNAPSHOT"
  ) dependsOn sync

}
import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "todo"
  val appVersion      = "1.0-SNAPSHOT"

  val main = play.Project(appName, appVersion).settings(
    templatesTypes <<= (templatesTypes) (_ orElse {
      case "js" => ("templates.JavaScript", "templates.JavaScriptFormat")
    }),
    libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.9",
    scalacOptions ++= Seq("-Xlint", "-feature")
  )

}

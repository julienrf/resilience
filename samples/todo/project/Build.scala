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
    libraryDependencies ++= Seq(
      "org.reactivemongo" %% "play2-reactivemongo" % "0.9",
      "fr.inria.powerapi" % "library" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.sensor" % "sensor-cpu-proc-times" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.formula" % "formula-cpu-max" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.sensor" % "sensor-mem-proc" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.formula" % "formula-mem-single" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.processor" % "device-aggregator" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.reporter" % "reporter-file" % "1.6-SNAPSHOT",
      "fr.inria.powerapi.reporter" % "reporter-jfreechart" % "1.6-SNAPSHOT"
    ),
    scalacOptions ++= Seq("-Xlint", "-feature"),
    resolvers += "Local M2" at Path.userHome.asURL + ".m2/repository"
  )

}

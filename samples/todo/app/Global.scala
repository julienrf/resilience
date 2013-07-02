
import scala.concurrent.duration.DurationInt
import fr.inria.powerapi.core.Process
import fr.inria.powerapi.library.PowerAPI
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {

  lazy val process = {
    /*scala.util.Try {
      Process(Integer.parseInt(scalax.io.Resource.fromFile("RUNNING_PID").string()))
    }.getOrElse(sys.error("Unable to find the PID of the application"))*/
    Process(java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split("@")(0).toInt)
  }

  val modules = Array(
    classOf[fr.inria.powerapi.sensor.cpu.proc.times.CpuSensor],
    classOf[fr.inria.powerapi.formula.cpu.max.CpuFormula],
    classOf[fr.inria.powerapi.sensor.mem.proc.MemSensor],
    classOf[fr.inria.powerapi.formula.mem.single.MemFormula]
  )

  override def onStart(app: Application) {
    super.onStart(app)
    modules.foreach(PowerAPI.startEnergyModule)
    PowerAPI.startMonitoring(
      process = process,
      duration = 500.milliseconds,
      processor = classOf[fr.inria.powerapi.processor.aggregator.device.DeviceAggregator],
      listener = classOf[fr.inria.powerapi.reporter.jfreechart.JFreeChartReporter]
    )
  }

  override def onStop(app: Application) {
    PowerAPI.stopMonitoring(process)
    modules.foreach(PowerAPI.stopEnergyModule)
    super.onStop(app)
  }

}

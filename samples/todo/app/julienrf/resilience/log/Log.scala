package julienrf.resilience.log

import scala.concurrent.Future
import julienrf.resilience.Event

trait Log extends Event {

  def log: LogLike

  trait LogLike {

    def exists(event: Event): Future[Boolean]

    def append(event: Event): Future[Unit]

    def history(): Future[Seq[Event]]

  }
}

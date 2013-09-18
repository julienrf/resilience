package julienrf.resilience.log

import scala.concurrent.Future
import julienrf.resilience.Event

/**
 * Stores the succession of events.
 * All events are timestamped (ordered),
 */
trait Log extends Event {

  def log: LogLike

  trait LogLike {

    /**
     * @return `event` has already been appended to the log
     */
    def exists(event: Event): Future[Boolean]

    /**
     * @param event Event to append to the log
     * @return A logical timestamp
     */
    def append(event: Event): Future[Double]

    /**
     * @param since Timestamp
     * @return The ordered sequence of events (along with their timestamp) that happened after `since`
     */
    def history(since: Option[Double] = None): Future[Seq[(Double, Event)]]

  }
}

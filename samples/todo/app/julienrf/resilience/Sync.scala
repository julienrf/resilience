package julienrf.resilience

import play.api.libs.iteratee.{Enumerator, Iteratee, Concurrent}
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}
import julienrf.resilience.log.Log

/**
 * Synchronizes between clients and servers
 */
trait Sync extends Event with Log {

  def sync: Sync

  /**
   * @param interprete Domain interpretation of events (TODO Handle failure)
   */
  class Sync(interprete: Event => Future[Unit])(implicit ec: ExecutionContext) {

    private val (_notifications, channel) = Concurrent.broadcast[Seq[Event]]

    // Output stream of events
    val notifications = _notifications

    private val atomicallyApply = Iteratee.foldM(()) { (_, event: Event) =>
      for {
        // Check that the event has not already been applied
        exists <- log.exists(event)
        if !exists
        // Apply the event to the application’s state
        _ <- interprete(event)
        // Append it to the log
        _ <- log.append(event)
        // Notify each client
        _ = channel.push(Seq(event))
      } yield ()
    }

    // We receive commands through this iteratee
    val commands = Iteratee.foreach[Seq[Event]](events => Enumerator(events: _*) |>> atomicallyApply)

    // Recover state. HACK Should be synchronous
    for {
      events <- log.history()
      event <- events
    } interprete(event)

  }

}

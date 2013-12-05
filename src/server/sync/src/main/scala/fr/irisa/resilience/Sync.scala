package fr.irisa.resilience

import play.api.libs.iteratee._
import scala.concurrent.{ExecutionContext, Future}
import fr.irisa.resilience.log.Log
import scala.concurrent.stm.Ref
import play.api.Logger
import java.util.UUID
import scala.annotation.tailrec

/**
 * Synchronizes between clients and servers
 */
trait Sync extends Event with Log {

  /**
   * A notification is an event paired with a logical timestamp
   */
  type Notifications = Seq[(Double, Event)]

  /**
   * A set of events paired with the last known event timestamp
   */
  type Commands = (Double, Seq[Event])

  def sync: Sync

  /**
   * @param interprete Domain interpretation of events (TODO Handle failure)
   */
  class Sync(interprete: Event => Future[Unit])(implicit ec: ExecutionContext) {

    /**
     * The default implementation returns `e1`, so it does *not* resolve conflicts at all.
     * @return An event `e1′` such that `interprete(e2) ; interprete(e1′)` converges with the effect of `interprete(e1) ; interprete(e2)`
     */
    def converge(e1: Event, e2: Event): Option[Event] = Some(e1) // FIXME Leave it abstract?

    private val clients = Ref(Map.empty[String, Concurrent.Channel[Notifications]])

    // Recover state. HACK Should be synchronous
    val stateRecovered = for {
      events <- log.history()
      _ <- events.foldLeft(Future.successful(()))((f, event) => f.flatMap(_ => interprete(event._2)))
    } yield ()

    private val atomicallyApply = Iteratee.foldM(()) { (_, event: Event) =>
      for {
        // Check that the event has not already been applied
        alreadyApplied <- log.exists(event)
        if !alreadyApplied
        // Apply the event to the application’s state
        _ <- interprete(event)
        // Append it to the log
        time <- log.append(event)
        // Notify each client
        _ = clients.single().values.foreach(_.push(Seq(time -> event)))
      } yield ()
    }

    private val convergingEvents: Enumeratee[(Double, Seq[Event]), Seq[Event]] =
      Enumeratee.mapM[(Double, Seq[Event])] { case (timestamp, events) =>

        @tailrec
        def convergeLoop(events: Seq[Event], priorEvents: Seq[Event]): Seq[Event] = priorEvents match {
          case Nil => events
          case event +: priorEvents => convergeLoop(events.flatMap(e => converge(e, event)), priorEvents)
        }

        for (history <- log.history(Some(timestamp))) yield convergeLoop(events, history.map(_._2))
      }

    private val flatten: Enumeratee[Seq[Event], Event] =
      Enumeratee.mapFlatten[Seq[Event]](Enumerator(_: _*))

    // We receive commands through this iteratee
    private val commands: Iteratee[Commands, Unit] =
      convergingEvents ><> flatten &>> atomicallyApply

    /**
     * @return A tuple containing an iteratee consuming commands sent by the joining client and an enumerator pushing notifications to this client
     */
    def join(): (Iteratee[Commands, Unit], Enumerator[Notifications]) = {
      val id = UUID.randomUUID().toString
      val getMissingUpdates = Enumeratee.map[Commands] { cs =>
        val timestamp = cs._1
        for (notifications <- log.history(Some(timestamp)) if notifications.nonEmpty) {
          clients.single()(id).push(notifications)
        }
        cs
      }
      val commandsConsumer = getMissingUpdates &>> commands.map { _ =>
        Logger.debug(s"unsubscribe [$id]")
        clients.single.transform(_ - id)
      }
      val notificationsPusher = Concurrent.unicast[Notifications](onStart = { channel =>
        clients.single.transform(_ + (id -> channel))
        Logger.debug(s"subscribe [$id]")
      })
      (commandsConsumer, notificationsPusher)
    }

    /**
     * Imperatively applies commands
     * @param cs Commands to apply
     */
    def applyCommands(timestamp: Double, cs: Event*): Unit = commands.feed(Input.El(timestamp -> cs))

  }

}

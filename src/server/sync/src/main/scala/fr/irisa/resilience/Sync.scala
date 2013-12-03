package fr.irisa.resilience

import play.api.libs.iteratee.{Enumeratee, Enumerator, Iteratee, Concurrent}
import scala.concurrent.{ExecutionContext, Future}
import fr.irisa.resilience.log.Log
import scala.concurrent.stm.Ref
import play.api.Logger
import java.util.UUID

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

    private val clients = Ref(Map.empty[String, Concurrent.Channel[Notifications]])

    /**
     * @return A tuple containing an iteratee consuming commands sent by the joining client and an enumerator pushing notifications to this client
     */
    def join(): (Iteratee[Commands, Unit], Enumerator[Notifications]) = {
      val id = UUID.randomUUID().toString
      val clientCommands = commands.map { _ =>
        Logger.debug(s"unsubscribe [$id]")
        clients.single.transform(_ - id)
      }
      val clientNotifications = Concurrent.unicast[Notifications](onStart = { channel =>
        clients.single.transform(_ + (id -> channel))
        Logger.debug(s"subscribe [$id]")
      })
      (clientCommands, clientNotifications)
    }

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

    // TODO transform events to achieve convergence
    private val convergentEvents: Enumeratee[(Double, Seq[Event]), Seq[Event]] =
      Enumeratee.map[(Double, Seq[Event])] { case (timestamp, events) => events }

    private val flatten: Enumeratee[Seq[Event], Event] =
      Enumeratee.mapFlatten[Seq[Event]](Enumerator(_: _*))

    // We receive commands through this iteratee
    val commands: Iteratee[Commands, Unit] =
      convergentEvents ><> flatten &>> atomicallyApply

    // Recover state. HACK Should be synchronous
    for {
      events <- log.history()
      _ <- events.foldLeft(Future.successful(()))((f, event) => f.flatMap(_ => interprete(event._2)))
    } yield ()

  }

}

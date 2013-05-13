package controllers

import akka.actor.{Props, Actor}
import play.api.libs.iteratee.{Enumerator, Iteratee, Concurrent}
import business.{State, Event}
import play.api.libs.concurrent.Akka
import scala.concurrent.Future
import akka.util.Timeout
import java.util.concurrent.TimeUnit

trait Sync {

  // Domain events
  type Events
  // Domain interpretation of events
  def interprete(events: Events)

  class SyncActor extends Actor {

    val (notifications, channel) = Concurrent.broadcast[Events]

    // A batch of events has to be applied
    def apply(events: Events) {
      interprete(events)
      // Notify each client of the applied actions
      channel.push(events)
    }

    def receive = {
      // A new client joins
      case Sync.Join() =>
        // We will interprete his actions using this iteratee
        val interpreter = Iteratee.foreach[Events] { apply(_) }
        // Send back the interpreter and the event notification stream
        sender ! (interpreter, notifications)

    }

  }

}

object Sync extends Sync {

  case class Join()

  // TODO Find a better place for the following code
  import play.api.Play.current
  import akka.pattern.ask

  private val sync = Akka.system.actorOf(Props(new SyncActor))

  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  def join: Future[(Iteratee[Events, _], Enumerator[Events])] =
    (sync ? Join()).mapTo[(Iteratee[Events, _], Enumerator[Events])]

  type Events = Seq[Event]

  def interprete(events: Events) {
    // Apply each event (TODO handle failure)
    events.foreach(State.apply)
  }

}
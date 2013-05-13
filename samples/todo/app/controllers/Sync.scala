package controllers

import akka.actor.{Props, Actor}
import play.api.libs.iteratee.{Enumerator, Iteratee, Concurrent}
import business.{State, Event}
import play.api.libs.concurrent.Akka
import scala.concurrent.Future
import akka.util.Timeout
import java.util.concurrent.TimeUnit

// TODO Generalize over the events domain and the state updates
class Sync extends Actor {

  import Sync._

  val (notifications, channel) = Concurrent.broadcast[Seq[Event]]

  // A batch of events has to be applied
  def apply(events: Seq[Event]) {
    // Apply each event (TODO handle failure)
    events.foreach(State.apply)
    // Notify each client of the applied actions
    channel.push(events)
  }

  def receive = {
    // A new client joins
    case Join() =>
      // We will interprete his actions using this iteratee
      val interpreter = Iteratee.foreach[Seq[Event]] { apply(_) }
      // Send back the interpreter and the event notification stream
      sender ! (interpreter, notifications)

  }

}

object Sync {

  case class Join()

  // TODO Find a better place for the following code
  import play.api.Play.current
  import akka.pattern.ask

  private val sync = Akka.system.actorOf(Props[Sync])

  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  type Events = Seq[Event]

  def join: Future[(Iteratee[Events, _], Enumerator[Events])] =
    (sync ? Join()).mapTo[(Iteratee[Events, _], Enumerator[Events])]

}
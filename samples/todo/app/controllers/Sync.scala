package controllers

import play.api.libs.iteratee.{Iteratee, Concurrent}
import business.{State, Event}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.json.{JsObject, Writes, Reads, Json}
import scala.concurrent.ExecutionContext
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.stm.Ref
import play.api.Logger

trait Sync {

  // Domain events
  type Events
  // Domain interpretation of events
  def interprete(events: Events)

  protected def journalCollection: JSONCollection
  protected implicit def executionContext: ExecutionContext
  protected implicit def eventsRead: Reads[Events]
  protected implicit def eventsWrite: Writes[Events]

  val (notifications, channel) = Concurrent.broadcast[Events]
  // We will interprete his actions using this iteratee
  val interpreter = Iteratee.foreach[Events] { apply(_) }
  private val id = Ref(0)

  // A batch of events has to be applied
  def apply(events: Events): Unit = {
    journalCollection
      .save(Json.obj("id" -> id.single.getAndTransform(_ + 1), "data" -> Json.toJson(events)))
      .filter(_.ok)
      .onFailure { case error => Logger.debug(s"There was a problem: $error") }
    interprete(events)
    // Notify each client of the applied actions
    channel.push(events)
  }

  // Recover state
  for {
    eventsList <- journalCollection.find(Json.obj()).sort(Json.obj("id" -> 1)).cursor[JsObject].toList
    entry <- eventsList
    events <- (entry \ "data").validate[Events]
  } interprete(events)

}

object Sync extends Sync {

  // TODO Find a better place for the following code
  import play.api.Play.current
  import JsonProtocols.{readEvent, writeEvent}
  lazy val executionContext = play.api.libs.concurrent.Execution.defaultContext
  lazy val eventsRead = Reads.seq[Event]
  lazy val eventsWrite = Writes.seq[Event]

  lazy val journalCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("resilience_todo")

  type Events = Seq[Event]

  def interprete(events: Events) {
    // Apply each event (TODO handle failure)
    events.foreach(State.apply)
  }

}
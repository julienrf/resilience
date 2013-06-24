package julienrf.resilience

import play.api.libs.iteratee.{Iteratee, Concurrent}
import play.api.libs.json.{JsObject, Writes, Reads, Json}
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.stm.Ref

trait Sync {

  def sync: Sync

  trait Sync {

    // Domain events
    type Event
    // Domain interpretation of events (FIXME Make it asynchronous? TODO Handle failure)
    def interprete(event: Event): Unit

    // JSON serialization and MongoDB collection (TODO abstract over the journal implementation)
    protected def journalCollection: JSONCollection
    protected implicit def executionContext: ExecutionContext
    protected implicit def eventsRead: Reads[Event]
    protected implicit def eventsWrite: Writes[Event]

    private val (_notifications, channel) = Concurrent.broadcast[Seq[Event]]
    private val time = Ref(0)

    // A batch of events has to be applied
    def apply(events: Seq[Event]): Unit = {
      for (event <- events) {
        // Apply the event to the application’s state
        interprete(event)
        // Log the event in the journal
        journalCollection
          .save(Json.obj("time" -> time.single.getAndTransform(_ + 1), "event" -> Json.toJson(event)))
          .filter(_.ok)
          .onFailure { case error => Logger.debug(s"There was a problem: $error") }
      }
      // Notify each client of the applied actions
      channel.push(events)
    }

    // Recover state
    def recover(): Future[Unit] =
      for (eventsList <- journalCollection.find(Json.obj()).sort(Json.obj("time" -> 1)).cursor[JsObject].toList) yield {
        for {
          entry <- eventsList
          event <- (entry \ "event").validate[Event]
        } interprete(event)
      }

    // Output stream of events
    val notifications = _notifications
    // We receive commands through this iteratee
    val commands = Iteratee.foreach(apply)
  }

}

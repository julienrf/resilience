package julienrf.resilience

import play.api.libs.iteratee.{Enumerator, Iteratee, Concurrent}
import play.api.libs.json.{JsObject, Writes, Reads, Json}
import play.api.Logger
import reactivemongo.core.commands.{Update, FindAndModify}
import reactivemongo.bson.BSONDocumentWriter
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.ImplicitBSONHandlers._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.stm.Ref

trait EventLike {
  def id: String
}

trait Sync {

  def sync: Sync

  trait Sync {

    // Domain events
    type Event <: EventLike
    // Domain interpretation of events (FIXME Make it asynchronous? TODO Handle failure)
    def interprete(event: Event): Unit

    // JSON serialization and MongoDB collection (TODO abstract over the journal implementation)
    protected def journalCollection: JSONCollection
    protected implicit def executionContext: ExecutionContext
    protected implicit def eventsRead: Reads[Event]
    protected implicit def eventsWrite: Writes[Event]

    private val (_notifications, channel) = Concurrent.broadcast[Seq[Event]]
    private val time = Ref(0)

    // Atomically apply an event (two events can not be applied concurrently, they are totally ordered)
    private val atomicallyApply = Iteratee.foldM(()){ (_, event: Event) =>
    //Logger.debug(s"""findAnModify({ "query": { event.id: "${event.id}" }, "update": {${Json.toJson(event)} })""")
      journalCollection.db.command(FindAndModify(
        journalCollection.name,
        query = implicitly[BSONDocumentWriter[JsObject]].write(Json.obj("event.id" -> event.id)),
        modify = Update(implicitly[BSONDocumentWriter[JsObject]].write(Json.obj("time" -> time.single.transformAndGet(_ + 1), "event" -> Json.toJson(event))), fetchNewObject = false),
        upsert = true
      )).transform(_ match {
        case Some(oldEntry) =>
          if (oldEntry.isEmpty) {
            // Apply the event to the application’s state
            interprete(event)
            // Notify each client of the applied actions
            channel.push(Seq(event))
          }
        case None => Logger.debug("Problem?")
      }, error => {
        Logger.debug(s"There was a problem: $error")
        error
      }
      )
    }

    // Output stream of events
    val notifications = _notifications
    // We receive commands through this iteratee
    val commands = Iteratee.foreach[Seq[Event]](events => Enumerator(events: _*) |>> atomicallyApply)

    // Recover state
    for (eventsList <- journalCollection.find(Json.obj()).sort(Json.obj("time" -> 1)).cursor[JsObject].toList) {
      for {
        entry <- eventsList
        last <- eventsList.map(entry => (entry \ "time").asOpt[Int]).max
        event <- (entry \ "event").validate[Event]
      } {
        interprete(event)
        time.single.set(last)
      }
    }
  }
}

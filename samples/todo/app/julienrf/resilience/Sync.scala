package julienrf.resilience

import play.api.libs.iteratee.{Iteratee, Concurrent}
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

    // Applies a batch of events
    private def apply(events: Seq[Event]): Unit = events match {
      case event +: events =>
        Logger.debug(s"""findAnModify({ "query": { event.id: "${event.id}" }, "update": {${Json.toJson(event)} })""")
        journalCollection.db.command(FindAndModify(
          journalCollection.name,
          query = implicitly[BSONDocumentWriter[JsObject]].write(Json.obj("event.id" -> event.id)),
          modify = Update(implicitly[BSONDocumentWriter[JsObject]].write(Json.obj("time" -> time.single.getAndTransform(_ + 1), "event" -> Json.toJson(event))), fetchNewObject = false),
          upsert = true
        )).map { maybeOldEntry =>
            maybeOldEntry match {
              case Some(oldEntry) =>
                if (oldEntry.isEmpty) {
                  // Apply the event to the application’s state
                  interprete(event)
                  // Notify each client of the applied actions
                  channel.push(Seq(event))
                }
              case None => Logger.debug("Problem?")
            }
          }
          .onFailure {
            case error => Logger.debug(s"There was a problem: $error")
          }
        apply(events)
      case _ =>
    }

    // Recover state
    def recover(): Future[Unit] =
      for (eventsList <- journalCollection.find(Json.obj()).sort(Json.obj("time" -> 1)).cursor[JsObject].toList) yield {
        for {
          entry <- eventsList
          last <- eventsList.map(entry => (entry \ "time").asOpt[Int]).max
          event <- (entry \ "event").validate[Event]
        } {
          interprete(event)
          time.single.set(last)
        }
      }

    // Output stream of events
    val notifications = _notifications
    // We receive commands through this iteratee
    val commands = Iteratee.foreach(apply)
  }

}

package business

import play.api.libs.json.{Writes, Reads}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import julienrf.resilience.Sync

object Todo extends State with Sync {

  val state = new State(new Items(Nil))

  val sync = new Sync {
    // Events are just a Seq of domain Event
    type Event = business.Event

    // JSON serialization configuration and MongoDB collection
    val executionContext = play.api.libs.concurrent.Execution.defaultContext
    val eventsRead = JsonProtocols.readEvent
    val eventsWrite = JsonProtocols.writeEvent
    val journalCollection = ReactiveMongoPlugin.db(play.api.Play.current).collection[JSONCollection]("resilience_todo")

    // Domain interpretation of events
    def interprete(event: Event): Unit = {
      // Apply each event
      state.exec(event)
    }
  }

  sync.recover()

}
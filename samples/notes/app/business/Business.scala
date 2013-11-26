package business

import fr.irisa.resilience.{Sync, State}
import fr.irisa.resilience.log.MongoDBLog
import scala.concurrent.Future
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Business extends State with Sync with MongoDBLog with JsonProtocols {

  type Domain = Note

  val state = new State(Note(""), e => n => interpreter(e)(n))

  val log = new MongoDBLog(ReactiveMongoPlugin.db(play.api.Play.current).collection[JSONCollection]("resilience_notes"))

  val sync = new Sync(e => Future.successful(state.apply(e)))

}

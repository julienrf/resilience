package business

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import fr.irisa.resilience.{Sync, State}
import fr.irisa.resilience.log.MongoDBLog
import scala.concurrent.Future

object Todo extends State with Sync with MongoDBLog with Interpreter with JsonProtocols {

  type Domain = Items

  val state = new State(new Items(Nil), e => is => interpreter(e)(is))

  val log = new MongoDBLog(ReactiveMongoPlugin.db(play.api.Play.current).collection[JSONCollection]("resilience_todo"))

  val sync = new Sync(event => Future.successful(state.apply(event)))

}

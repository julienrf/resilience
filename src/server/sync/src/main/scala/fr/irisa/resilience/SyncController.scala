package fr.irisa.resilience

import play.api.mvc.{Action, WebSocket, Controller}
import play.api.libs.json._
import scala.concurrent.ExecutionContext
import scala.util.Try

trait SyncController extends Controller {

  private implicit def readTuple2[A : Reads, B : Reads] = Reads[(A, B)] { json =>
    for {
      array <- json.validate[Seq[JsValue]]
      tuple <- Try {
        for {
          a <- array(0).validate[A]
          b <- array(1).validate[B]
        } yield (a, b)
      }.getOrElse(JsError())
    } yield tuple
  }
  private implicit def writeTuple2[A : Writes, B : Writes] = Writes[(A, B)] ( o =>  Json.arr(o._1, o._2) )

  def WebSocketSync(sync: Sync)(implicit format: Format[sync.Event], ec: ExecutionContext) = WebSocket.using[JsValue] { _ =>
    val (commands, notifications) = sync.sync.join()
    (Json.fromJson[sync.Commands] &>> commands, notifications &> Json.toJson[sync.Notifications])
  }

  def getHistory(sync: Sync, since: Option[Double])(implicit writes: Writes[sync.Event], ec: ExecutionContext) = Action.async {
    sync.log.history(since) map (es => Ok(Json.toJson(es)))
  }

}

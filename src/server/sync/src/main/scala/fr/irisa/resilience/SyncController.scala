package fr.irisa.resilience

import play.api.mvc.{Action, WebSocket, Controller}
import play.api.libs.json.{Format, Writes, Json, JsValue}
import scala.concurrent.ExecutionContext

trait SyncController extends Controller {

  private implicit def writeTuple2[A : Writes, B : Writes] = Writes[(A, B)] ( o =>  Json.arr(o._1, o._2) )

  def WebSocketSync(sync: Sync)(implicit format: Format[sync.Event]) = WebSocket.using[JsValue] { _ =>
    (Json.fromJson[Seq[sync.Event]] &>> sync.sync.commands, sync.sync.notifications &> Json.toJson[Seq[(Double, sync.Event)]])
  }

  def getHistory(sync: Sync, since: Option[Double])(implicit writes: Writes[sync.Event], ec: ExecutionContext) = Action.async {
    sync.log.history(since) map (es => Ok(Json.toJson(es)))
  }

}

import akka.actor.ActorSystem
import business.{Toggled, Added}
import com.ning.http.client.AsyncHttpClient
import com.ning.http.client.websocket.{WebSocket, WebSocketUpgradeHandler, WebSocketTextListener}
import java.util.concurrent.Future
import java.util.UUID
import play.api.libs.json.Json
import business.JsonProtocols._
import scala.concurrent.duration.DurationInt
import scala.util.Try

object Main extends App {

  val url = Try(args(0)) getOrElse "ws://localhost:9000/sync-2"
  val http = new AsyncHttpClient()
  val system = ActorSystem()
  import system.dispatcher

  def ws(start: WebSocket => Unit = _ => ()): Future[WebSocket] =
    http.prepareGet(url).execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
      new WebSocketTextListener() {
        def onMessage(message: String): Unit = ()
        def onOpen(ws: WebSocket): Unit = start(ws)
        def onClose(ws: WebSocket): Unit = ()
        def onError(t: Throwable): Unit = println(s"Error: $t")
        def onFragment(fragment: String, last: Boolean): Unit = ()
      }).build())

  system.scheduler.schedule(0.second, 2.seconds) {
    // Create 10 passive clients
    println(s"Adding 40 clients")
    for (_ <- 1 to 39) {
      ws()
    }

    // Create a client that adds an item to the list
    val itemId = UUID.randomUUID().toString
    ws { self =>
      //println(s"Adding an item")
      self.sendTextMessage(Json.arr(Json.toJson(Added(UUID.randomUUID().toString, itemId, "foo", done = false))).toString)
      system.scheduler.schedule(1.second, 1.second) {
        //println("Toggling an item")
        self.sendTextMessage(Json.arr(Json.toJson(Toggled(UUID.randomUUID().toString, itemId))).toString)
      }
    }
  }

}

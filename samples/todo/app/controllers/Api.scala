package controllers

import play.api.mvc.{WebSocket, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, JsValue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import business._
import JsonProtocols._
import play.api.Logger

object Api extends Controller {

  val add = Action { implicit request =>
    Form(tuple(
      "id" -> nonEmptyText,
      "itemId" -> nonEmptyText,
      "content" -> nonEmptyText,
      "done" -> boolean
    )).bindFromRequest().fold(
      _ => BadRequest,
      { case (id, itemId, content, done) =>
        State.apply(Added(id, itemId, content, done))
        Ok
      }
    )
  }

  def remove(id: String, itemId: String) = Action { implicit request =>
    State.apply(Removed(id, itemId))
    Ok
  }

  def toggle(id: String, itemId: String) = Action { implicit request =>
    State.apply(Toggled(id, itemId))
    Ok
  }

  /**
   * Apply a batch of domain events to the application state.
   */
  val sync = Action(parse.json) { implicit request =>
    val eventsApplied =
      for (events <- request.body.validate[Seq[Event]]) yield {
        events.foreach(State.apply)
        Ok
      }
    eventsApplied recoverTotal { _ =>
      Logger.warn("Unable to parse events")
      BadRequest
    }
  }

  /**
   * A websocket entry point to apply batches of events and receive notifications from other clients actions
   */
  val sync2 = WebSocket.async[JsValue] { _ =>
    for ((interpreter, notifications) <- Sync.join)
    yield (Json.fromJson[Seq[Event]] &>> interpreter, notifications &> Json.toJson[Seq[Event]])
  }

}

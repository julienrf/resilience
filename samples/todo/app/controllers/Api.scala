package controllers

import play.api.mvc.{WebSocket, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, JsValue}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import business.Todo
import play.api.cache.Cached
import play.api.Play.current
import scala.concurrent.duration.DurationInt

object Api extends Controller {

  import Todo.{Added, Removed, Toggled, Event}
  import Todo.protocols._

  val add = Action { implicit request =>
    Form(tuple(
      "id" -> nonEmptyText,
      "itemId" -> nonEmptyText,
      "content" -> nonEmptyText,
      "done" -> boolean
    )).bindFromRequest().fold(
      _ => BadRequest,
      { case (id, itemId, content, done) =>
        Todo.state.exec(Added(id, itemId, content, done))
        Ok
      }
    )
  }

  def remove(id: String, itemId: String) = Action { implicit request =>
    Todo.state.exec(Removed(id, itemId))
    Ok
  }

  def toggle(id: String, itemId: String) = Action { implicit request =>
    Todo.state.exec(Toggled(id, itemId))
    Ok
  }

  /**
   * Apply a batch of domain events to the application state.
   */
  val sync = Action(parse.json) { implicit request =>
    val eventsApplied =
      for (events <- request.body.validate[Seq[Event]]) yield {
        events.foreach(Todo.state.exec)
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
  val sync2 = WebSocket.using[JsValue] { _ =>
    (Json.fromJson[Seq[Event]] &>> Todo.sync.commands, Todo.sync.notifications &> Json.toJson[Seq[(Double, Event)]])
  }


  def history(since: Option[Double]) = Action.async {
    Todo.log.history(since) map (es => Ok(Json.toJson(es)))
  }

  val about = Cached("Api.about") {
    Action {
      Ok(Json.obj("content" -> "Resilient TodoMVC implementation"))
    }
  }

}

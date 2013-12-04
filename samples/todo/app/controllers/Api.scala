package controllers

import play.api.mvc.Action
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import business.Todo
import play.api.cache.Cached
import play.api.Play.current
import fr.irisa.resilience.SyncController

object Api extends SyncController {

  import Todo.{Added, Removed, Toggled}
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
        Todo.sync.applyCommands(-1, Added(id, itemId, content, done))
        Ok
      }
    )
  }

  def remove(id: String, itemId: String) = Action { implicit request =>
    Todo.sync.applyCommands(-1, Removed(id, itemId))
    Ok
  }

  def toggle(id: String, itemId: String) = Action { implicit request =>
    Todo.sync.applyCommands(-1, Toggled(id, itemId))
    Ok
  }

  /**
   * A websocket entry point to apply batches of events and receive notifications from other clients actions
   */
  val sync = WebSocketSync(Todo)

  def history(since: Option[Double]) = getHistory(Todo, since)

  val about = Cached("Api.about") {
    Action {
      Ok(Json.obj("content" -> "Resilient TodoMVC implementation"))
    }
  }

}

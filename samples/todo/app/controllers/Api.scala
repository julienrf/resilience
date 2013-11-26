package controllers

import play.api.mvc.Action
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.Logger

import business.Todo
import play.api.cache.Cached
import play.api.Play.current
import fr.irisa.resilience.SyncController

object Api extends SyncController {

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
        Todo.state.apply(Added(id, itemId, content, done))
        Ok
      }
    )
  }

  def remove(id: String, itemId: String) = Action { implicit request =>
    Todo.state.apply(Removed(id, itemId))
    Ok
  }

  def toggle(id: String, itemId: String) = Action { implicit request =>
    Todo.state.apply(Toggled(id, itemId))
    Ok
  }

  /**
   * Apply a batch of domain events to the application state.
   */
  val sync = Action(parse.json) { implicit request =>
    val eventsApplied =
      for (events <- request.body.validate[Seq[Event]]) yield {
        events.foreach(Todo.state.apply)
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
  val sync2 = WebSocketSync(Todo)

  def history(since: Option[Double]) = getHistory(Todo, since)

  val about = Cached("Api.about") {
    Action {
      Ok(Json.obj("content" -> "Resilient TodoMVC implementation"))
    }
  }

}

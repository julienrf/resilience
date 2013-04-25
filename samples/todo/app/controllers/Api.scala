package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Enumerator
import business._
import JsonProtocols._
import play.api.Logger

object Api extends Controller {

  val add = Action { implicit request =>
    Form(tuple(
      "id" -> nonEmptyText,
      "content" -> nonEmptyText,
      "done" -> boolean
    )).bindFromRequest().fold(
      _ => BadRequest,
      { case (id, content, done) =>
        State.apply(Added(id, content, done))
        Ok
      }
    )
  }

  def remove(id: String) = Action { implicit request =>
    State.apply(Removed(id))
    Ok
  }

  def toggle(id: String) = Action { implicit request =>
    State.apply(Toggled(id))
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
   * An event source of updates that have been applied to the application state.
   */
  val updates = Action {
    Ok.feed(Enumerator[JsValue]()).as(EVENT_STREAM)
  }
}

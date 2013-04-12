package controllers

import play.api.mvc.{Action, Controller}
import play.api.data.Form
import play.api.data.Forms._
import business.{Toggled, Removed, Added, State}

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

}

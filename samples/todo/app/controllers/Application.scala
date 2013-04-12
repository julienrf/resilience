package controllers

import play.api.mvc.{Action, Controller}

object Application extends Controller {

  val index = Action {
    Ok(views.html.main())
  }

}
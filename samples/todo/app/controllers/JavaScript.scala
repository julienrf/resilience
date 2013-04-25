package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import business.State

object JavaScript extends Controller {

  val main = Action {
    Ok(views.js.main(State.current))
  }

  val routes = Action { implicit request =>
    import controllers.routes.javascript.Api
    val jsRoutes = Routes.javascriptRouter("routes")(
      Api.add,
      Api.remove,
      Api.toggle,
      Api.sync
    )
    Ok(s"define(function () { $jsRoutes; return routes });").as(JAVASCRIPT)
  }

}

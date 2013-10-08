package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import business.Todo
import play.api.libs.json.Json

object JavaScript extends Controller {

  val routes = Action { implicit request =>
    import controllers.routes.javascript.Api
    val jsRoutes = Routes.javascriptRouter("routes")(
      // Api.add,
      // Api.remove,
      // Api.toggle,
      // Api.sync,
      Api.sync2,
      Api.history,
      Api.about
    )
    Ok(s"define(function () { $jsRoutes; return routes });").as(JAVASCRIPT)
  }

}

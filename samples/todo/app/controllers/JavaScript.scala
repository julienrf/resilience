package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import business.Todo

object JavaScript extends Controller {

  val main = Action {
    Ok(views.js.main(Todo.state.current))
  }

  val routes = Action { implicit request =>
    import controllers.routes.javascript.Api
    val jsRoutes = Routes.javascriptRouter("routes")(
      // Api.add,
      // Api.remove,
      // Api.toggle,
      Api.sync,
      Api.sync2
    )
    Ok(s"define(function () { $jsRoutes; return routes });").as(JAVASCRIPT)
  }

}

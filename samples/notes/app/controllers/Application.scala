package controllers

import play.api.mvc.Action
import play.api.Routes
import business.Business
import fr.irisa.resilience.SyncController
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends SyncController {

  val index = Action(Ok(views.html.index()))

  val javascriptRouter = Action { implicit request =>
    val router = Routes.javascriptRouter("routes")(
      routes.javascript.Application.sync,
      routes.javascript.Application.history
    )
    Ok(s"define(function () { $router; return routes })").as(JAVASCRIPT)
  }

  val sync = WebSocketSync(Business)

  def history(lastId: Option[Double]) = getHistory(Business, lastId)

}
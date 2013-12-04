package controllers

import play.api.mvc.{Action, Controller}
import play.api.cache.Cached
import play.api.Play.current
import scala.concurrent.duration.DurationInt
import play.api.Play

object Application extends Controller {

  val index = Cached("index.html", 1.day.toSeconds.toInt) {
    Action {
      Ok(views.html.main())
    }
  }

  val cacheManifest =
    if (Play.current.configuration.getBoolean("resilience.appcache").getOrElse(false))
      Cached("todo.manifest", 1.day.toSeconds.toInt) {
        Action { implicit request =>
          Ok(
            s"""CACHE MANIFEST
            |# v1
            |${routes.Assets.at("stylesheets/base.min.css").url}
            |${routes.Assets.at("stylesheets/todo.min.css").url}
            |${routes.Assets.at("stylesheets/bg.png").url}
            |${routes.Assets.at("javascripts/require.js").url}
            |${routes.Assets.at("javascripts/control.js").url}
            |${routes.Assets.at("javascripts/business.js").url}
            |${routes.Assets.at("javascripts/ui.js").url}
            |${routes.Assets.at("javascripts/resilience-sync.js").url}
            |${routes.Assets.at("javascripts/events.js").url}
            |${routes.Assets.at("javascripts/lib/uuid.js").url}
            |${routes.Assets.at("javascripts/lib/el.js").url}
            |${routes.Assets.at("javascripts/resilience-http.js").url}
            |${routes.Assets.at("javascripts/main.js").url}
            |${routes.Assets.at("javascripts/routes.js").url}
            |NETWORK:
            |*
            |SETTINGS:
            |prefer-online
          """.stripMargin).as("text/cache-manifest")
        }
      }
    else Action(NotFound)

}
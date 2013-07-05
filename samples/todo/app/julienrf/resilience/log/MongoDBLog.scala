package julienrf.resilience.log

import julienrf.resilience.Event

import scala.concurrent.{Future, ExecutionContext}

import play.api.libs.json._
import play.api.libs.json.JsObject

import play.modules.reactivemongo.json.collection.JSONCollection

trait MongoDBLog extends Log with Event {

  def log: MongoDBLog

  class MongoDBLog(collection: JSONCollection)(implicit ec: ExecutionContext, reads: Reads[Event], writes: Writes[Event]) extends LogLike {

    def exists(event: Event): Future[Boolean] =
      for {
        maybeEvent <- collection.find(Json.obj("event.id" -> event.id)).one
      } yield maybeEvent.isDefined

    def append(event: Event): Future[Unit] =
      for {
        maybeLastEvent <- collection.find(Json.obj()).sort(Json.obj("time" -> -1)).one[JsObject]
        time = maybeLastEvent flatMap (lastEvent => (lastEvent \ "time").asOpt[Double] map (_ + 1)) getOrElse 0.0
        _ <- collection.save(Json.obj("time" -> time, "event" -> Json.toJson(event)))
      } yield ()

    def history(): Future[Seq[Event]] =
      for (eventsList <- collection.find(Json.obj()).sort(Json.obj("time" -> 1)).cursor[JsObject].toList) yield {
        for {
          entry <- eventsList
          event <- (entry \ "event").validate[Event].asOpt
        } yield event
      }

  }

}

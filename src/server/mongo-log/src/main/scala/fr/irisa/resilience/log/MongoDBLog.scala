package fr.irisa.resilience.log

import fr.irisa.resilience.Event

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

    def append(event: Event): Future[Double] =
      for {
        maybeLastEvent <- collection.find(Json.obj()).sort(Json.obj("time" -> -1)).one[JsObject]
        time = maybeLastEvent flatMap (lastEvent => (lastEvent \ "time").asOpt[Double] map (_ + 1)) getOrElse 0.0
        _ <- collection.save(Json.obj("time" -> time, "event" -> Json.toJson(event)))
      } yield time

    def history(since: Option[Double] = None): Future[Seq[(Double, Event)]] = {
      val request = since match {
        case Some(timestamp) => Json.obj("time" -> Json.obj("$gt" -> since))
        case None => Json.obj()
      }
      for (eventsList <- collection.find(request).sort(Json.obj("time" -> 1)).cursor[JsObject].collect[List]()) yield {
        for {
          entry <- eventsList
          time <- (entry \ "time").asOpt[Double]
          event <- (entry \ "event").asOpt[Event]
        } yield time -> event
      }
    }

  }

}

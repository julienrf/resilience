package controllers

import play.api.libs.json._
import play.api.libs.json.Json.JsValueWrapper
import business._

object JsonProtocols {

  implicit val writeItem = Writes[Item] { item =>
    Json.obj(
      "id" -> item.id,
      "content" -> item.content,
      "done" -> item.done
    )
  }

  implicit val writeItems = Writes[Items] { items =>
    Json.arr(items.items.map(i => (i: JsValueWrapper)): _*)
  }

  implicit val readEvent = Reads[Event] { js =>
    for {
      tag <- (js \ "tag").validate[String]
      event <- readFromTag(tag, js)
    } yield event
  }

  def readFromTag(tag: String, js: JsValue): JsResult[Event] = tag match {
    case "Added" =>
      for {
        id <- (js \ "itemId").validate[String]
        content <- (js \ "content").validate[String]
        done <- (js \ "done").validate[Boolean]
      } yield Added(id, content, done)
    case "Removed" =>
      for {
        id <- (js \ "itemId").validate[String]
      } yield Removed(id)
    case "Toggled" =>
      for {
        id <- (js \ "itemId").validate[String]
      } yield Toggled(id)
  }
}

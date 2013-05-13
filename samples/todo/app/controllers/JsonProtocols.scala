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
      id <- (js \ "id").validate[String]
      event <- readFromTag(tag, id, js)
    } yield event
  }

  def readFromTag(tag: String, id: String, js: JsValue): JsResult[Event] = tag match {
    case "Added" =>
      for {
        itemId <- (js \ "itemId").validate[String]
        content <- (js \ "content").validate[String]
        done <- (js \ "done").validate[Boolean]
      } yield Added(id, itemId, content, done)
    case "Removed" =>
      for {
        itemId <- (js \ "itemId").validate[String]
      } yield Removed(id, itemId)
    case "Toggled" =>
      for {
        itemId <- (js \ "itemId").validate[String]
      } yield Toggled(id, itemId)
  }

  implicit val writeEvent = Writes[Event] {
    case Added(id, itemId, content, done) =>
      Json.obj("tag" -> "Added", "id" -> id, "itemId" -> itemId, "content" -> content, "done" -> done)
    case Removed(id, itemId) =>
      Json.obj("tag" -> "Removed", "id" -> id, "itemId" -> itemId)
    case Toggled(id, itemId) =>
      Json.obj("tag" -> "Toggled", "id" -> id, "itemId" -> itemId)
  }

}

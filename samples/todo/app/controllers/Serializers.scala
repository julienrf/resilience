package controllers

import play.api.libs.json.{Json, Writes}
import business.{Item, Items}
import play.api.libs.json.Json.JsValueWrapper

object Serializers {

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

}

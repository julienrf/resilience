package business

import play.api.libs.json._
import play.api.libs.json.Json.JsValueWrapper

trait JsonProtocols extends Events {

  object protocols {

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

}

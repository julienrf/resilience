package business

import play.api.libs.json._

trait JsonProtocols extends Events {

  implicit val noteFormat = Json.format[Note]

  implicit val charInsertedFormat = Json.format[CharInserted]

  implicit val charRemovedFormat = Json.format[CharRemoved]

  implicit val eventFormat = Format[Event](
    Reads { json =>
      for {
        tag <- (json \ "tag").validate[String]
        event <- tag match {
          case "CharInserted" => json.validate[CharInserted]
          case "CharRemoved" => json.validate[CharRemoved]
        }
      } yield event
    },
    Writes {
      case CharInserted(id, str, pos) =>
        Json.obj("tag" -> "CharInserted", "id" -> id, "str" -> str, "pos" -> pos)
      case CharRemoved(id, pos) =>
        Json.obj("tag" -> "CharRemoved", "id" -> id, "pos" -> pos)
    }
  )

}

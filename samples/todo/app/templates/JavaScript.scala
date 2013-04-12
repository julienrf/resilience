package templates

import play.templates.Format
import play.api.http.{ContentTypeOf, MimeTypes, ContentTypes, Writeable}
import play.api.mvc.{Content, Codec}
import org.apache.commons.lang3.StringEscapeUtils

class JavaScript extends play.templates.Appendable[JavaScript] with Content {

  private val buffer = new StringBuilder

  def this(content: String) = {
    this()
    buffer.append(content)
  }

  def body: String = buffer.toString()

  override def toString = body

  def contentType: String = MimeTypes.JAVASCRIPT

  def +=(other: JavaScript): JavaScript = {
    buffer.append(other.buffer)
    this
  }

}

object JavaScript {

  def apply(text: String) = new JavaScript(text)

  implicit def contentType(implicit codec: Codec): ContentTypeOf[JavaScript] =
    ContentTypeOf(Some(ContentTypes.JAVASCRIPT))

}

object JavaScriptFormat extends Format[JavaScript] {

  def raw(text: String): JavaScript = JavaScript(text)

  def escape(text: String): JavaScript = JavaScript(StringEscapeUtils.escapeEcmaScript(text))

}
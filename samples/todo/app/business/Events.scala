package business

import fr.irisa.resilience.Event
import julienrf.variants.Variants

trait Events extends Event {

  sealed trait Event extends EventLike
  case class Added(id: String, itemId: String, content: String, done: Boolean) extends Event
  case class Removed(id: String, itemId: String) extends Event
  case class Toggled(id: String, itemId: String) extends Event

  object Event {
    implicit val eventFormat = Variants.format[Event]
  }

}
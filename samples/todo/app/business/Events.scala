package business

import julienrf.resilience.Event

trait Events extends Event {

  sealed trait Event extends EventLike
  case class Added(id: String, itemId: String, content: String, done: Boolean) extends Event
  case class Removed(id: String, itemId: String) extends Event
  case class Toggled(id: String, itemId: String) extends Event

}
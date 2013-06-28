package business

sealed trait Event extends julienrf.resilience.EventLike
case class Added(id: String, itemId: String, content: String, done: Boolean) extends Event
case class Removed(id: String, itemId: String) extends Event
case class Toggled(id: String, itemId: String) extends Event
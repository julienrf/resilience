package business

sealed trait Event
case class Added(id: String, itemId: String, content: String, done: Boolean) extends Event
case class Removed(id: String, itemId: String) extends Event
case class Toggled(id: String, itemId: String) extends Event
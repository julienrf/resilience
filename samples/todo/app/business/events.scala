package business

sealed trait Event
case class Added(id: String, content: String, done: Boolean) extends Event
case class Removed(id: String) extends Event
case class Toggled(id: String) extends Event
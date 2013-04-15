package business

import play.api.Logger

class Interpreter {
  def apply(event: Event)(state: Items): Items = event match {
    case Added(id, content, done) =>
      state.copy(items = state.items :+ Item(id, content, done)) // Han, append is bad
    case Removed(id) =>
      state.copy(items = state.items.filterNot(_.id == id))
    case Toggled(id) =>
      state.items.find(_.id == id) match {
        case Some(item) =>
          state.copy(items = (state.items.takeWhile(_ != item) :+ item.toggle) ++ state.items.dropWhile(_ != item).tail)
        case None =>
          Logger.warn(s"Item $id not found")
          state
      }
  }
}

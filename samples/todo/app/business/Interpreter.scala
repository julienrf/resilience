package business

import play.api.Logger

class Interpreter {

  def apply(event: Event)(state: Items): Items = event match {
    case Added(id, itemId, content, done) =>
      state.copy(items = state.items :+ Item(itemId, content, done)) // Han, append is bad
    case Removed(id, itemId) =>
      state.copy(items = state.items.filterNot(_.id == itemId))
    case Toggled(id, itemId) =>
      state.items.find(_.id == itemId) match {
        case Some(item) =>
          state.copy(items = (state.items.takeWhile(_ != item) :+ item.toggle) ++ state.items.dropWhile(_ != item).tail)
        case None =>
          Logger.warn(s"Item $id not found")
          state
      }
  }

}

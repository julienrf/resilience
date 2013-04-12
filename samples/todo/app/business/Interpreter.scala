package business

class Interpreter {
  def apply(event: Event)(state: Items): Items = event match {
    case Added(id, content, done) =>
      state.copy(items = state.items :+ Item(id, content, done)) // Han, append is bad
    case Removed(id) =>
      state.copy(items = state.items.filterNot(_.id == id))
    case Toggled(id) =>
      val i = state.items.indexWhere(_.id == id)
      if (i < 0) sys.error("That’s bad.")
      val item = state.items(i)
      state.copy(items = state.items.updated(i, item.copy(done = !item.done)))
  }
}

package business

case class Item(id: String, content: String, done: Boolean) {
  def toggle = copy(done = !done)
}

case class Items(items: List[Item])
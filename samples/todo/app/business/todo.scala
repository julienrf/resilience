package business

case class Item(id: String, content: String, done: Boolean)

case class Items(items: List[Item])
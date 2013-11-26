package business

case class Note(content: String) {

  def insert(str: String, pos: Int) = { // FIXME Throw an exception if `pos` is out of `content` bounds
    val (prefix, suffix) = content.splitAt(pos)
    copy(content = prefix ++ str ++ suffix)
  }

  def remove(pos: Int) = {
    copy(content = content.take(pos) ++ content.drop(pos + 1)) // FIXME Check bounds
  }

}

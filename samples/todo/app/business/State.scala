package business

import scala.concurrent.stm.Ref
import java.util.UUID

class State(interpreter: Interpreter, initVal: Items) {

  private val todos = Ref(initVal)

  def apply(event: Event) {
    todos.single.transform(interpreter(event))
  }

  def current: Items = todos.single()
}

// In-memory application state
object State extends State(
  new Interpreter,
  new Items(List(
    Item(UUID.randomUUID().toString, "foo", done = false),
    Item(UUID.randomUUID().toString, "bar", done = true)))
)
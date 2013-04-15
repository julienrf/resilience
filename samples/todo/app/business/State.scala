package business

import scala.concurrent.stm.Ref
import play.api.Logger

class State(interpreter: Interpreter, initVal: Items) {

  private val todos = Ref(initVal)

  def apply(event: Event) {
    Logger.debug(event.toString)
    todos.single.transform(interpreter(event))
  }

  def current: Items = todos.single()
}

// In-memory application state
object State extends State(new Interpreter, new Items(Nil))
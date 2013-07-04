package business

import scala.concurrent.stm.Ref
import play.api.Logger

/**
 * In-memory state, derived from the application of the domain events
 */
trait State extends Interpreter {

  def state: State

  class State(initVal: Items) {

    private val todos = Ref(initVal)

    def exec(event: Event) {
      Logger.debug(event.toString)
      todos.single.transform(interpreter(event))
    }

    def current: Items = todos.single()

  }

}

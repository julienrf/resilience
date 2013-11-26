package business

import fr.irisa.resilience.Event

trait Events extends Event {

  sealed trait Event extends EventLike
  case class CharInserted(id: String, str: String, pos: Int) extends Event
  case class CharRemoved(id: String, pos: Int) extends Event

  object interpreter {

    def apply(e: Event)(state: Note): Note = e match {
      case CharInserted(_, char, pos) =>
        state.insert(char, pos)
      case CharRemoved(_, pos) =>
        state.remove(pos)
    }

  }

}

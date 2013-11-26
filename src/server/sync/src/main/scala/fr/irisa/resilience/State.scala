package fr.irisa.resilience

import scala.concurrent.stm.Ref

/**
 * In-memory application state
 */
trait State extends Event {

  type Domain
  def state: State

  class State(init: Domain, interpreter: Event => Domain => Domain) {

    private val state = Ref(init)

    def apply(event: Event): Unit = {
      state.single.transform(interpreter.apply(event))
    }

    def current: Domain = state.single()

  }

}

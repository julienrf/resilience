package fr.irisa.resilience

trait Event {

  type Event <: EventLike

  // HACK This trait exists only to make things easier for the mongodb log
  trait EventLike {
    def id: String
  }

}

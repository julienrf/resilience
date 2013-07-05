package julienrf.resilience

trait Event {

  type Event <: EventLike

  trait EventLike {
    def id: String
  }

}

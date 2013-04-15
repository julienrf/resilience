define(['lib/uuid'], (uuid) ->

  class Toggled
    constructor: (@itemId) ->
      @tag = "Toggled"
    accept: (visitor) -> visitor.toggled(@itemId)

  class Added
    constructor: (@itemId, @content, @done) ->
      @tag = "Added"
    accept: (visitor) -> visitor.added(@itemId, @content, @done)

  class Removed
    constructor: (@itemId) ->
      @tag = "Removed"
    accept: (visitor) -> visitor.removed(@itemId)

  {
    Toggled: Toggled,
    Added: Added,
    Removed: Removed,
    toggle: ((id) -> new Toggled(id)),
    add: ((content, done) -> new Added(uuid(), content, done)),
    remove: ((id) -> new Removed(id))
  }
)
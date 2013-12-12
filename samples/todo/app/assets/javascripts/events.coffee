define(['lib/uuid'], (uuid) ->

  {
    toggle: ((id) ->
      $variant: 'Toggled'
      id: uuid()
      itemId: id
    )
    add: ((content, done) ->
      $variant: 'Added'
      id: uuid()
      itemId: uuid()
      content: content
      done: done
    )
    remove: ((id) ->
      $variant: 'Removed'
      id: uuid()
      itemId: id
    )
    fold: (event) -> (f) ->
      if event.$variant == 'Toggled'
        f.Toggled(event.itemId)
      else if event.$variant == 'Added'
        f.Added(event.itemId, event.content, event.done)
      else if event.$variant == 'Removed'
        f.Removed(event.itemId)
  }
)
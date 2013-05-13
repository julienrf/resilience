define(['lib/uuid'], (uuid) ->

  {
    toggle: ((id) ->
      tag: 'Toggled'
      id: uuid()
      itemId: id
    )
    add: ((content, done) ->
      tag: 'Added'
      id: uuid()
      itemId: uuid()
      content: content
      done: done
    )
    remove: ((id) ->
      tag: 'Removed'
      id: uuid()
      itemId: id
    )
    fold: (event) -> (f) ->
      if event.tag == 'Toggled'
        f.Toggled(event.itemId)
      else if event.tag == 'Added'
        f.Added(event.itemId, event.content, event.done)
      else if event.tag == 'Removed'
        f.Removed(event.itemId)
  }
)
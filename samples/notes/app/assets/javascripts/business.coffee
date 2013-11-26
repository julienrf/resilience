define(() ->

  class Note
    constructor: (@content) ->
    insert: (str, pos) ->
      prefix = @content.substring(0, pos)
      suffix = @content.substring(pos)
      @content = prefix + str + suffix
    remove: (pos) ->
      @content = @content.substring(0, pos) + @content.substring(pos + 1)

  class Notes
    constructor: (@notes) ->
    add: (id, note) ->
      @notes[id] = note
    remove: (id) ->
      delete @notes[id]
    get: (id) ->
      maybeNote = @notes[id]
      if maybeNote then [maybeNote] else []

  {
    Note: Note
    Notes: Notes
  }
)
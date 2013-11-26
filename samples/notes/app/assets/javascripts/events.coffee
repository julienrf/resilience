define(['uuid'], (uuid) ->

  {
    charInserted: (str, pos) ->
      tag: 'CharInserted'
      id: uuid()
      str: str
      pos: pos

    charRemoved: (pos) ->
      tag: 'CharRemoved'
      id: uuid()
      pos: pos

    noteAdded: (noteId) ->
      tag: 'NoteAdded'
      id: uuid()
      noteId: noteId

    noteRemoved: (noteId) ->
      tag: 'NoteRemoved'
      id: uuid()
      noteId: noteId

    fold: (event) -> (f) ->
      if event.tag == 'CharInserted'
        f.CharInserted(event.str, event.pos)
      else if event.tag == 'CharRemoved'
        f.CharRemoved(event.pos)
      else if event.tag == 'NoteAdded'
        f.NoteAdded(event.noteId)
      else if event.tag == 'NoteRemoved'
        f.NoteRemoved(event.noteId)

  }

)
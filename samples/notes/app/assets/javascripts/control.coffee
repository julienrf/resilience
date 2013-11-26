define(['business', 'ui', 'events', 'resilience-sync', 'uuid', 'routes'], (business, ui, events, sync, uuid, routes) ->

  class Note extends business.Note
    constructor: (@interpreter, content) ->
      super(content)
      @ui = new ui.Note(this, @interpreter.ui.root, @content)
    onKeyDown: (e, pos) ->
      console.log(e)
      console.log(window.getSelection())
      if e.keyCode == 46
        @interpreter.apply(events.charRemoved(pos))
      else
        @interpreter.apply(events.charInserted(String.fromCharCode(e.keyCode), pos))
      e.preventDefault()
      e.stopPropagation()
    insert: (str, pos) ->
      super(str, pos)
      @ui.update(@content)
    remove: (pos) ->
      super(pos)
      @ui.update(@content)

#  class Notes extends business.Notes
#    constructor: (@interpreter, notes) ->
#      super(notes)
#    newDocumentClicked: () ->
#      @interpreter.apply(events.noteAdded(uuid()))

  class Interpreter extends sync.ctl.Sync
    constructor: (@state, syncRoute, historyRoute) ->
      super(syncRoute, historyRoute, "notes")
    interprete: (event) ->
      events.fold(event)(
        CharInserted: (str, pos) =>
          @state().insert(str, pos)

        CharRemoved: (pos) =>
          @state().remove(pos)

#        NoteAdded: (noteId) =>
#          @state.add(noteId, new Note(''))
#
#        NoteRemoved: (noteId) =>
#          @state.remove(noteId)
      )

  class App
    constructor: () ->
      @interpreter = new Interpreter((() => @state), routes.controllers.Application.sync(), routes.controllers.Application.history)
      @state = new Note(@interpreter, '')
      @ui = @state.ui


  {
    App: App
  }
)
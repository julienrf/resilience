define(['el'], (el) ->

  class Note
    constructor: (ctl, sync, content) ->
      @editor = el('div', { 'contenteditable': 'true' })(content)
      @root = el('div')(
        @editor,
        sync
      )
      @editor.addEventListener('keydown', (e) -> ctl.onKeyDown(e, window.getSelection().anchorOffset))
    update: (content) ->
      pos = window.getSelection().anchorOffset
      @editor.textContent = content
      window.getSelection().removeAllRanges()
      range = document.createRange()
      range.setStart((if @editor.childNodes.length > 0 then @editor.childNodes[0] else @editor), pos)
      range.collapse(true)
      window.getSelection().addRange(range)

  class Notes
    constructor: (ctl) ->
      @root = el('button')('Create a new document')
      @root.addEventListener('click', () -> ctl.newDocumentClicked())

  {
    Note: Note
    Notes: Notes
  }

)
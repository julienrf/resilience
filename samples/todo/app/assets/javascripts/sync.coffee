define(['events', '/assets/routes.js'], (events, routes) ->

  Ajax = {
    call: (settings) ->
      xhr = new XMLHttpRequest
      xhr.open(settings.route.method, settings.route.url)
      data = new FormData
      for k, v of settings.data
        data.append(k, v)
      xhr.send(data)
      xhr
  }

  class Interpreter
    constructor: () ->
      @queue = []

    apply: (event) ->
      ajax = (settings) =>
        xhr = Ajax.call(settings)
        xhr.addEventListener('load', () =>
          @popUpTo(event)
        )
        xhr.addEventListener('error', () =>
          console.log('Oops.')
        )
      @push(event)
      event.accept({
        toggled: (itemId) ->
          ajax({
            route: routes.controllers.Api.toggle(itemId)
          })

        added: (itemId, content, done) ->
          ajax({
            route: routes.controllers.Api.add(),
            data: {
              id: itemId,
              content: content,
              done: done
            }
          })

        removed: (itemId) ->
          ajax({
            route: routes.controllers.Api.remove(itemId)
          })
      })

    popUpTo: (event) ->
      removed = @queue.shift()
      if event != removed
        @popUpTo(event)

    push: (event) ->
      @queue.push(event)


  {
    Sync: Interpreter
  }
)
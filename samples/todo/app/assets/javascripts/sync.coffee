define(['events', '/assets/routes.js'], (events, routes) ->

  Ajax = {
    call: (settings) ->
      xhr = new XMLHttpRequest
      xhr.open(settings.route.method, settings.route.url)
      xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8')
      data = if settings.data != undefined then JSON.stringify(settings.data) else null
      xhr.send(data)
      xhr
  }

  class Interpreter
    constructor: () ->
      @queue = []

    apply: (event) ->
      @push(event)
      @sync(event)

    sync: (event) ->
      ajax = (settings) =>
        xhr = Ajax.call(settings)
        xhr.addEventListener('load', () =>
          if xhr.status >= 400
            console.log('Error', xhr.statusText)
          else
            console.log('Success', xhr.statusText)
            @popUpTo(event)
        )
        xhr.addEventListener('error', () =>
          console.log('The connection to the service has been lost.')
        )
      ajax({
        route: routes.controllers.Api.batch(),
        data: @queue
      })
      ###
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
      ###

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
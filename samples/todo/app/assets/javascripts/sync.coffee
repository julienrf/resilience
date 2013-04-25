define(() ->

  Ajax = {
    call: (settings) ->
      xhr = new XMLHttpRequest
      xhr.open(settings.route.method, settings.route.url)
      xhr.setRequestHeader('Content-Type', 'application/json;charset=UTF-8')
      data = if settings.data != undefined then JSON.stringify(settings.data) else null

      xhr.addEventListener('load', () =>
        if xhr.status >= 400
          settings.failure(xhr) if settings.failure?
        else
          settings.success(xhr) if settings.success?
      )
      xhr.addEventListener('error', () =>
        settings.error(xhr) if settings.error?
      )

      xhr.send(data)
      xhr
  }

  # Method `interprete :: Event -> ()` is abstract and must be implemented to interprete the event in terms of business logic
  class Interpreter
    constructor: (@syncRoute) ->
      @queue = []

    apply: (event) ->
      @push(event)
      @sync() # We try to sync on each user action. TODO sync only if the server is not under a heavy load
      @interprete(event)

    sync: () ->
      if @queue.length > 0
        last = @queue[@queue.length - 1]
        Ajax.call({
          route: @syncRoute,
          data: @queue,
          success: (xhr) =>
            @popUpTo(last)
          error: (xhr) =>
            @connectionLost()
          failure: (xhr) =>
            console.log('Failure', xhr.statusText)
        })

    popUpTo: (event) ->
      i = @queue.indexOf(event)
      if i >= 0
        @queue.splice(0, i + 1)

    push: (event) ->
      @queue.push(event)

    connectionLost: () ->
      if not @_pending?
        @_pending = setTimeout(() =>
          delete @_pending
          @sync()
        , 10000)


  {
    Sync: Interpreter
  }
)
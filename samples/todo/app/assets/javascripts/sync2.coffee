define(() ->

  # Method `interprete :: Event -> ()` is abstract and must be implemented to interprete the event in terms of business logic
  # FIXME Make `interprete` a constructor parameter
  class Interpreter
    constructor: (@syncRoute) ->
      @queue = []
      @ws = @makeWS(@syncRoute.webSocketURL())

    makeWS: (url) ->
      ws = new WebSocket(url)
      ws.addEventListener('message', (m) =>
        events = JSON.parse(m.data)
        for event in events
          if @queue.some((e) -> e.id == event.id)
            @remove(event)
          else
            @interprete(event)
      )
      ws.addEventListener('open', () => console.log('open', arguments); @connectionRecovered())
      ws.addEventListener('error', () => console.log('error', arguments); @connectionLost()) # TODO Handle errors
      ws.addEventListener('close', () => console.log('close', arguments); @connectionLost()) # TODO?
      ws

    apply: (event) ->
      @push(event)
      @sync() # We try to sync on each user action. TODO sync only if the server is not under a heavy load
      @interprete(event)

    sync: () ->
      if @queue.length > 0 and @ws and @ws.readyState == WebSocket.OPEN
        @ws.send(JSON.stringify(@queue))

    remove: (event) ->
      @queue = @queue.filter((e) -> e.id != event.id)

    push: (event) ->
      @queue.push(event)

    connectionLost: () ->
      if not @timeout?
        delete @ws
        @timeout = 10000 # TODO Make the timeout grow exponentially
        setTimeout(() =>
          delete @timeout
          @ws = @makeWS(@syncRoute.webSocketURL())
        , @timeout)

    connectionRecovered: () ->
      @sync()



  {
    Sync: Interpreter
  }
)
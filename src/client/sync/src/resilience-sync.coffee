define(['resilience-http'], (http) ->

  # Method `interprete :: Event -> ()` is abstract and must be implemented to interprete the event in terms of business logic
  # FIXME Make `interprete` a constructor parameter
  class Interpreter
    constructor: (@syncRoute, historyRoute, dbName) ->
      @queue = []
      @ws = @makeWS(@syncRoute.webSocketURL())
      @idb = []
      r = window.indexedDB.open(dbName)
      r.addEventListener('success', (e) =>
        db = e.target.result
        @idb.push(db)
        # load local journal
        tx = db.transaction(['log'])
        req = tx.objectStore('log').openCursor(IDBKeyRange.lowerBound(0))
        maxIdx = null
        req.addEventListener('success', (e) =>
          result = e.target.result
          if result != null
            @interprete(result.value.event)
            maxIdx = result.value.time
            e.target.result.continue()
        )
        req.addEventListener('error', (e) -> console.log(e))
        # load new events from server
        tx.addEventListener('complete', (e) =>
          http.get(historyRoute(maxIdx).url)
            .foreach((es) => es.forEach((e) => @execute(e[0], e[1])))
        )
      )
      r.addEventListener('upgradeneeded', (e) =>
        db = e.target.result
        db.createObjectStore('log', { keyPath: 'time' })
      )

    makeWS: (url) ->
      ws = new WebSocket(url)
      ws.addEventListener('message', (m) =>
        timestampedEvents = JSON.parse(m.data)
        for timestampedEvent in timestampedEvents
          time = timestampedEvent[0]
          event = timestampedEvent[1]
          if @queue.some((e) -> e.id == event.id)
            @remove(event)
            @log(time, event)
          else
            @execute(time, event)
      )
      ws.addEventListener('open', () => console.log('open', arguments); @connectionRecovered())
      ws.addEventListener('error', () => console.log('error', arguments); @connectionLost()) # TODO Handle errors
      ws.addEventListener('close', () => console.log('close', arguments); @connectionLost()) # TODO?
      ws

    # Apply an event that has been initiated locally
    apply: (event) ->
      @push(event)
      @sync(true) # We try to sync on each user action. TODO sync only if the server is not under a heavy load
      @interprete(event)

    # Try to interprete an event and to log it
    execute: (time, event) ->
      @interprete(event)
      @log(time, event)

    sync: (optimistic) ->
      if @queue.length > 0 and @ws and @ws.readyState == WebSocket.OPEN
        @ws.send(JSON.stringify(if optimistic then [@queue[@queue.length - 1]] else @queue))

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

    log: (time, event) ->
      # HACK Handle emptiness of idb
      @idb.forEach((db) ->
        tx = db.transaction(["log"], "readwrite")
        req = tx.objectStore("log").put({
          time: time,
          event: event
        })
        req.addEventListener('error', (e) -> console.log(e))
      )



  {
    Sync: Interpreter
  }
)
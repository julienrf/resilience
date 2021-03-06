define(['business', 'ui', 'events', 'resilience-sync', 'routes', 'resilience-http'], (business, ui, events, sync, routes, http) ->

  class Item extends business.Item
    constructor: (@parent, @interpreter, id, name, done, visible) ->
      super(id, name, done)
      @ui = new ui.Item(this, name, done, visible)

    toggle: () ->
      super()
      @ui.updateDone(@done)
      @parent().itemToggled(this)

    checkChanged: () ->
      @interpreter.apply(events.toggle(@id))

    destroyClicked: () ->
      @interpreter.apply(events.remove(@id))

  class Filter
    constructor: () ->
      @ui = new ui.Filter(this)


  class Items extends business.Items
    constructor: (@interpreter, items) ->
      super(items)
      @currentFilter = 'all'
      @filter = new Filter
      @ui = new ui.Items(this, @filter.ui, item.ui for item in items, @interpreter.ui)
      @updateItems()

    add: (item) ->
      super(item)
      @ui.add(item.ui)
      @updateItems()

    remove: (item) ->
      super(item)
      @ui.remove(item.ui)
      @updateItems()

    itemToggled: (item) ->
      @updateItems()
      if @currentFilter == 'active'
        item.ui[if item.done then 'hide' else 'show']()
      else if @currentFilter == 'completed'
        item.ui[if item.done then 'show' else 'hide']()

    updateItems: () ->
      @ui.updateToggleAll(@allCompleted())
      @ui.updateLeftCounter(@active().length)
      @ui.updateClearBtn(@completed().length)
      if @items.length > 0
        @ui.showList()
      else
        @ui.hideList()

    inputSubmitted: (content) ->
      @interpreter.apply(events.add(content, false))
      @ui.clear()

    toggleAllChanged: (checked) ->
      if @allCompleted()
        for item in @items
          @interpreter.apply(events.toggle(item.id))
      else
        for item in @active()
          @interpreter.apply(events.toggle(item.id))

    clearCompletedClicked: () ->
      for item in @completed()
        @interpreter.apply(events.remove(item.id))

    showAll: () ->
      @currentFilter = 'all'
      @items.forEach((item) -> item.ui.show())
      @filter.ui.showAllSelected()

    filterActive: () ->
      @currentFilter = 'active'
      @items.forEach((item) ->
        if item.done
          item.ui.hide()
        else
          item.ui.show()
      )
      @filter.ui.showActiveSelected()

    filterCompleted: () ->
      @currentFilter = 'completed'
      @items.forEach((item) ->
        if item.done
          item.ui.show()
        else
          item.ui.hide()
      )
      @filter.ui.showCompletedSelected()

    aboutClicked: () ->
      http(routes.controllers.Api.about())
        .map((response) =>
          alert(response.content)
        ).recoverServerFailure((exception) ->
          console.log(exception)
        ).recoverNetworkFailure((exception) ->
          console.log(exception)
        )


  # Interprete domain event as state transitions
  class Interpreter extends sync.ctl.Sync

    constructor: (@items, syncRoute, historyRoute) ->
      super(syncRoute, historyRoute, "todo")

    interprete: ((event) -> events.fold(event)(
      Toggled: (itemId) =>
        # TODO handle find failure
        @items().find(itemId).forEach((item) -> item.toggle())

      Added: (itemId, content, done) =>
        @items().add(new Item(@items, this, itemId, content, done, @items().currentFilter != 'completed'))

      Removed: (itemId) =>
        @items().find(itemId).forEach((item) => @items().remove(item))

    ))


  # Entry point
  class App
    constructor: (data) ->
      @interpreter = new Interpreter((() => @items), routes.controllers.Api.sync(), routes.controllers.Api.history)
      items = data.map((item) => new Item((() => @items), @interpreter, item.id, item.content, item.done, true))
      @items = new Items(@interpreter, items)
      @ui = @items.ui
      window.addEventListener('hashchange',() =>
        @route()
      )
      @route()
      #http.onFailure((info) ->
      #  alert('Oops. A request to the server failed (' + info.url + ').')
      #)

    route: () ->
      switch window.location.hash
        when '#/' then @items.showAll()
        when '#/active' then @items.filterActive()
        when '#/completed' then @items.filterCompleted()


  {
    Item: Item,
    Items: Items,
    App: App
  }
)
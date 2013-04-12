define(() ->
  class Item
    constructor: (@id, @name, @done) ->

    toggle: () ->
      @done = not @done

  class Items
    constructor: (@items) ->

    add: (item) ->
      @items.push(item)

    remove: (item) ->
      @items = @items.filter((i) -> i != item)

    find: (id) ->
      @items.filter((item) -> item.id == id)

    active: () ->
      @items.filter((item) -> not item.done)

    completed: () ->
      @items.filter((item) -> item.done)

    allCompleted: () ->
      @active().length == 0

  {
    Item: Item,
    Items: Items
  }
)

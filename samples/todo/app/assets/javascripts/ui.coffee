define(['el'], (el) ->

  class Item
    constructor: (@ctl, name, done, visible) ->
      @checkbox = el('input', { 'type': 'checkbox', 'class': 'toggle' })()
      @checkbox.checked = done
      destroy = el('button', { 'class': 'destroy' })()
      # List items should get the class `editing` when editing and `completed` when marked as completed
      @root = el('li')(
        el('div', { 'class': 'view' })(
          @checkbox,
          el('label')(name),
          destroy
        ),
        el('input', { 'class': 'edit', value: name })()
      )
      if done
        @root.classList.add('completed')
      if not visible
        @root.classList.add('hidden')

      @checkbox.addEventListener('change', () => @ctl.checkChanged())
      destroy.addEventListener('click', () => @ctl.destroyClicked())

    updateDone: (done) ->
      @checkbox.checked = done
      if done
        @root.classList.add('completed')
      else
        @root.classList.remove('completed')

    show: () ->
      @root.classList.remove('hidden')

    hide: () ->
      @root.classList.add('hidden')

  class Filter
    constructor: (@ctl) ->
      @all = el('a', { 'class': 'selected', href: '#/' })('All')
      @active = el('a', { href: '#/active' })('Active')
      @completed = el('a', { href: '#/completed' })('Completed')
      @root = el('ul', { id: 'filters' })(
        el('li')(@all),
        el('li')(@active),
        el('li')(@completed)
      )

    showAllSelected: () ->
      @all.classList.add('selected')
      @active.classList.remove('selected')
      @completed.classList.remove('selected')

    showActiveSelected: () ->
      @all.classList.remove('selected')
      @active.classList.add('selected')
      @completed.classList.remove('selected')

    showCompletedSelected: () ->
      @all.classList.remove('selected')
      @active.classList.remove('selected')
      @completed.classList.add('selected')


  class Items
    constructor: (@ctl, filter, items) ->
      @toggleAll = el('input', { id: 'toggle-all', type: 'checkbox' })()
      @items = el('ul', { id: 'todo-list' })(item.root for item in items)
      @main = el('section', { id: 'main' })(
        @toggleAll,
        el('label', { 'for': 'toggle-all' })('Mark all as complete'),
        @items
      )
      @input = el('input', { id: 'new-todo', placeholder: 'What needs to be done?', autofocus: 'autofocus' })()
      @count = el('strong')()
      @clearBtn = el('button', { id: 'clear-completed' })()
      @footer = el('footer', { id: 'footer' })(
        el('span', { id: 'todo-count' })(@count, ' item(s) left'),
        filter.root,
        @clearBtn
      )
      @root = el('div')(
        el('header', { id: 'header' })(
          el('h1')('todos'),
          @input
        ),
        @main,
        @footer
      )

      @input.addEventListener('keydown', (e) =>
        if e.keyCode == 13 && @input.value.length > 0
          @ctl.inputSubmitted(@input.value)
      )

      @toggleAll.addEventListener('change', () =>
        @ctl.toggleAllChanged(@toggleAll.checked)
      )

      @clearBtn.addEventListener('click', () =>
        @ctl.clearCompletedClicked()
      )

    add: (item) ->
      @items.appendChild(item.root)

    remove: (item) ->
      @items.removeChild(item.root)

    clear: () ->
      @input.value = ''

    updateLeftCounter: (n) ->
      @count.textContent = n

    updateToggleAll: (checked) ->
      @toggleAll.checked = checked

    updateClearBtn: (n) ->
      @clearBtn.textContent = 'Clear completed (' + n + ')'
      @clearBtn.style.display = if n == 0 then 'none' else 'block'

    hideList: () ->
      @main.style.display = 'none'
      @footer.style.display = 'none'
    showList: () ->
      @main.style.display = 'block'
      @footer.style.display = 'block'

  class Sync
    updateSync: (queue) ->
      if queue.length == 0
        console.log('Everything is synced!')
      else
        console.log(queue.length + ' events have to be synced')

  {
    Item: Item,
    Items: Items,
    Filter: Filter,
    Sync: Sync
  }
)
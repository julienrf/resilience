require(['routes', 'control'], (routes, ctl) ->

  # Start the application with cached events
  todos = new ctl.App([])
  document.getElementById('todoapp').appendChild(todos.ui.root)

)
require(['routes', 'control', 'lib/react'], (routes, ctl, react) ->

  # Start the application with cached events
  todos = new ctl.App([])
  document.getElementById('todoapp').appendChild(todos.ui.root)

)
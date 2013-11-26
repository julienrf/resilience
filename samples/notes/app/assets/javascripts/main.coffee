require(['control'], (ctl) ->
  app = new ctl.App
  document.getElementById('app').appendChild(app.ui.root)
)
@(state: business.Items)

@import business.JsonProtocols._

require(['control'], function (ctl) {

  const todos = new ctl.App(@templates.JavaScript(play.api.libs.json.Json.toJson(state).toString));
  document.getElementById('todoapp').appendChild(todos.ui.root);

});
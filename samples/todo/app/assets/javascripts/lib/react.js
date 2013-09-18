define(function () {

  /**
   * @param {(a -> ()) -> ()} subscribe Function that takes an “observer” (i.e. a function that consumes a `a`)
   * @constructor
   */
  var Observable = function (subscribe) {
    this.subscribe = subscribe;
  };
  Observable.fn = Observable.prototype;
  Observable.fn.map = function (f) {
    var self = this;
    return new Observable(function (publish) {
      self.subscribe(function (a) {
        publish(f(a));
      });
    })
  };
  Observable.fn.merge = function (that) {
    var self = this;
    return new Observable(function (publish) {
      self.subscribe(function (_) { publish(_); });
      that.subscribe(function (_) { publish(_); });
    })
  };
  Observable.fn.filter = function (p) {
    var self = this;
    return new Observable(function (publish) {
      self.subscribe(function (e) { if (p(e)) publish(e); });
    })
  };
  var Folder = function (z) {
    this.state = z;
  };
  Folder.prototype.fold = function (f, a) {
    this.state = f(a, this.state);
  };
  Observable.fn.fold = function (b, f) {
    var self = this;
    return new Observable(function (publish) {
      var folder = new Folder(b);
      publish(folder.state);
      self.subscribe(function (a) {
        folder.fold(f, a);
        publish(folder.state);
      });
    })
  };

  var http = {
    'get': function (url) {
      return url
    },
    send: function (requests) {
      return new Observable(function (publish) {
        requests.subscribe(function (url) {
          var xhr = new XMLHttpRequest();
          xhr.open('GET', url);
          xhr.addEventListener('load', function () {
            publish(JSON.parse(xhr.responseText))
          });
          xhr.send();
        });
      })
    }
  };

  return {
    Observable: function (subscribe) {
      return new Observable(subscribe)
    },
    on: function (event, el) {
      return new Observable(function (publish) {
        (el || window).addEventListener(event, function (e) {
          publish(e);
        });
      })
    },
    every: function (delay) {
      return new Observable(function (publish) {
        setInterval(function () {
          publish(new Date);
        }, delay);
      });
    },
    mergeAll: function (os) {
      return new Observable(function (publish) {
        os.foreach(function (o) {
          o.subscribe(function (_) { publish(_); });
        });
      })
    },
    property: function () {
      var publish;
      return {
        put: function (v) {
          publish(v);
        },
        observable: new Observable(function (p) {
          publish = p;
        })
      }
    },
    pure: function (value) {
      return new Observable(function (publish) {
          publish(value);
      })
    },
    http: http
  }
});
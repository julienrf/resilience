/**
 * TODO
 *  - Use cache on failure
 */
define(function () {

  var Success = function (value) {
    this.value = value;
  };

  var Failure = function (exception) {
    this.exception = exception;
  };

  var Promise = function () {
    this.result = null;
    this.callbacks = [];
    var self = this;
    this.future = new Future(function (f) {
      if (self.result === null) {
        self.callbacks.push(f);
      } else {
        f(self.result);
      }
    });
  };
  Promise.prototype.complete = function (result) {
    if (this.result !== null) {
      throw 'This promise has already been completed';
    } else {
      this.result = result;
      this.callbacks.forEach(function (f) {
        f(result);
      })
    }
  };
  Promise.prototype.success = function (value) {
    this.complete(new Success(value));
  };
  Promise.prototype.failure = function (exception) {
    this.complete(new Failure(exception));
  };

  var Future = function (onComplete) {
    this.onComplete = onComplete;
  };
  /** Applies `f` to the result, if this one is successful (will not be called if the future fails) */
  Future.prototype.foreach = function (f) {
    this.onComplete(function (result) {
      if (result instanceof Success) {
        f(result.value);
      }
    });
  };
  /** Creates a new future by applying `f` to the successful result of this future. If this
   *  future is completed with an exception, the new future will also contain this exception */
  Future.prototype.map = function (f) {
    var p = new Promise();
    this.onComplete(function (result) {
      if (result instanceof Success) {
        p.success(f(result.value));
      } else {
        p.complete(result);
      }
    });
    return p.future
  };
  /** Creates a new future by applying `f` to the successful result of this future. If this future
   *  or the future returned by `f` is completed with an exception, the new future will also
   *  contain this exception */
  Future.prototype.flatMap = function (f) {
    var p = new Promise();
    this.onComplete(function (result) {
      if (result instanceof Success) {
        f(result.value).onComplete(function (result) {
          p.complete(result)
        });
      } else {
        p.complete(result);
      }
    });
    return p.future
  };
  Future.prototype.recover = function (f) {
    var p = new Promise();
    this.onComplete(function (result) {
      if (result instanceof Failure) {
        p.success(f(result.exception));
      }
    });
    return p.future
  };

  var http = function (config) {

    var method = config.method || 'GET';
    var url = config.url;
    var xhr = new XMLHttpRequest();

    xhr.open(method, url);

    var type = /*config.type ||*/ 'json'; // default type is JSON
    xhr.responseType = type;
    if (type === 'json') {
      xhr.setRequestHeader('Accept', 'application/json');
    } else if (type === 'document') {
      xhr.setRequestHeader('Accept', 'text/html, text/xml, application/xml');
    } else if (type === 'text') {
      xhr.setRequestHeader('Accept', 'text/plain, */*;q=0.1');
    }

    var response = new Promise();

    xhr.addEventListener('readystatechange', function () {
      if (xhr.readyState === XMLHttpRequest.DONE) {
        var status = xhr.status;
        // In case of success or client error we successfully complete the response promise
        if (status >= 200 && status < 500) {
          // unsupported responseType = 'json' fallback
          if (type === 'json' && xhr.responseType !== 'json') {
            response.success(JSON.parse(xhr.response));
          } else {
            response.success(xhr.response);
          }
        } else {
          // FIXME Use a chain of responsibility pattern to handle the failure?
          http.failureCallbacks.forEach(function (f) {
            f({
              url: url,
              status: xhr.status,
              text: xhr.statusText
            });
          });
          response.failure('HTTP request failed: ' + xhr.status + ' ' + xhr.statusText);
        }
      }
    });

    xhr.send(config.data);

    return response.future
  };

  http.failureCallbacks = [];

  http.onFailure = function (f) {
    http.failureCallbacks.push(f);
  };

  return http

});
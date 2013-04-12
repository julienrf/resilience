define(function () {

  var forEachIn = function (o, f) {
    for (var p in o) {
      if (o.hasOwnProperty(p)) {
        f(p, o[p]);
      }
    }
  };

  var merge = function (src, target) {
    forEachIn(src, function (n, v) { target[n] = v; });
  };

  /**
   * That’s not really a class in an Object Oriented meaning.
   * That’s more a way to encode record types with methods, with the ability to specialize (extend) a record type.
   * @param Type Type of this class. Possible types are `Clazz` or any value obtained with `<type>.extend(…)`
   * @param fields Fields of this class.
   * @return An instance of `Type`
   * @constructor
   */
  var Clazz = function (Type, fields) {
    if (!(this instanceof Type)) {
      return new Type(fields)
    }
    this.__clazz__ = Type;
    merge(fields, this);
  };

  /**
   * Convenient way to copy an instance of a class with some changes.
   * @param updates Changes to apply to this instance.
   * @return A new instance whose fields are the same as this fields, updated with `updates`.
   */
  Clazz.prototype.copy = function (updates) {
    var fields = {};
    forEachIn(this, function (n, v) { if (n !== '__clazz__') fields[n] = v; });
    merge(updates, fields);
    return new this.__clazz__(fields)
  };

  /**
   * Structural equality.
   * @param that
   */
  Clazz.prototype.equals = function (that) {
    if (this.__clazz__ !== that.__clazz__) {
      throw 'Please do not compare Oranges with Apples'
    }
    var eq = true;
    forEachIn(this, function (n, v) { if (v !== that[n]) eq = false });
    return eq
  };

  /**
   * Create subclass constructor. Subclasses share methods of the parent class.
   * @param methods Methods defined by the subclass.
   * @return {Function} Subclass constructor.
   */
  Clazz.extend = function (methods) {
    var Type = function (fields) {
      return Clazz.call(this, Type, fields)
    };
    Type.prototype = Object.create(this.prototype);
    Type.extend = this.extend;
    merge(methods, Type.prototype);
    return Type
  };

  return Clazz
});
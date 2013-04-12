define(function () {

  var List = {
    add: function (a) {
      return new Cons(a, this)
    },
    zip: function (bs) {
      if (this === Nil || bs === Nil) return Nil;
      else return this.as.zip(bs.as).add([this.a, bs.a]);
    },
    equals: function (that) {
      if (this === Nil && that === Nil) return true;
      else return this.a.equals(that.a) && this.as.equals(that.as)
    }
  };

  var Cons = function (a, as) {
    this.a = a;
    this.as = as;
  };
  Cons.prototype = Object.create(List);
  Cons.fn = Cons.prototype;

  var Nil = Object.create(List);

  Cons.fn.toString = function () {
    return this.a.toString() + ' :: ' + this.as.toString()
  };

  Nil.toString = function () {
    return 'Nil'
  };

  Cons.fn.map = function (f) {
    return this.as.map(f).add(f(this.a))
  };

  Nil.map = function (f) {
    return Nil
  };

  Cons.fn.flatMap = function (f) {
    var bs = f(this.a);
    return bs.concat(this.as.flatMap(f))
  };

  Nil.flatMap = function (f) {
    return Nil
  };

  Cons.fn.foreach = function (f) {
    f(this.a);
    this.as.foreach(f);
  };

  Nil.foreach = function (_) { };

  Cons.fn.filter = function (p) {
    if (p(this.a)) return this.as.filter(p).add(this.a);
    else return this.as;
  };

  Nil.filter = function (_) {
    return Nil
  };

  Cons.fn.fold = function (nil, cons) {
    return cons(this.a, this.as.fold(nil, cons))
  };

  Nil.fold = function (nil, cons) {
    return nil()
  };

  Cons.fn.concat = function (aas) {
    return this.as.concat(aas).add(this.a);
  };

  Nil.concat = function (aas) {
    return aas
  };

  Cons.fn.isEmpty = function () {
    return false
  };

  Nil.isEmpty = function () {
    return true
  };

  Cons.fn.append = function (aa) {
    return this.as.append(aa).add(this.a)
  };

  Nil.append = function (aa) {
    return new Cons(aa, Nil)
  };

  Cons.fn.remove = function (a) {
    if (this.a === a) return this.as;
    else return this.as.remove(a).add(this.a);
  };

  Nil.remove = function (a) {
    return Nil
  };

  Cons.fn.updated = function (oldA, newA) {
    if (this.a = oldA) return this.as.add(newA);
    else return this.as.updated(oldA, newA).add(this.a)
  };

  Nil.updated = function (_) {
    return Nil
  };

  // TODO Make it linear
  Cons.fn.reverse = function () {
    var self = this;
    return this.fold(
      function () { return Nil },
      function (a, sa) { return sa.append(a) }
    )
  };

  Nil.reverse = function () {
    return Nil
  };

  Cons.fn.size = function () {
    return 1 + this.as.size()
  };

  Nil.size = function () {
    return 0
  };

  Cons.fn.toArray = function () {
    var aas = this.as.toArray();
    aas.unshift(this.a);
    return aas
  };


  Nil.toArray = function () {
    return []
  };


  return {
    Cons: function (a, as) {
      return new Cons(a, as)
    },
    Nil: Nil,
    fromArray: function (as) {
      return as.reduceRight(function (as, a) { return new Cons(a, as) }, Nil)
    }
  }

});
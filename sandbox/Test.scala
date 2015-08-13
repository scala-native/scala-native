package test

class C(val x: Int, val y: Int) {
  var z = 3
  def foo =
    new C(x + y, x - y)
}

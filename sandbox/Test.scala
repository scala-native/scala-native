package test

class C(val x: Int, val y: Int) {
  def foo =
    new C(x + y, x - y)
}

package test

class C {
  def foo(x: Int, y: Int): Int = {
    var z = x - y
    if (x > 0)
      z += x
    else
      z -= y
    z
  }
}

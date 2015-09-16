package test

class Test {
  def foo(x: Int) = {
    var y = 0
    val z =
      if (x > 0) {
        y = 1
        -x
      }
      else {
        y = 2
        x
      }
    z + y
  }
}

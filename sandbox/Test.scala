package test

class C {
  def foo(): Int = {
    var x: Int = 0
    while (x < 10)
      x += 1
    x
  }
}

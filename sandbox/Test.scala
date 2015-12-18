package test

object Test {
  def f(x: Int, y: Int) = {
    val bar = 0
    val baz = bar + 1
    var foo = baz + 2
    if (x > 0)
      foo += x
    else
      foo -= y
    foo
  }
}

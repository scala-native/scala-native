package test

class C {
  def foo(x: Int, y: Int): Any = {
    val z = bar(x + y)
    z
  }

  def bar[T](x: T): T = x
}

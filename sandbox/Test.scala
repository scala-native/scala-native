package test

class C {
  def foo(x: Int, y: Int) =
    (if (x + y > 0) x * y else 0).toLong
}

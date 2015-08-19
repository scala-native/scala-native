package test

class C {
  def foo(x: Int, y: Int) =
    if (((x + y) * (x - y)) > 0) x else y
}

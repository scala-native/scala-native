package test

object M {
  def bar = 2
}

class C {
  def foo(a: Int) =
    if (M.bar == 2) 3 else 4
}

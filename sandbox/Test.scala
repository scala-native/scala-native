package test

class Test {
  def foo(x: Int): Int =
    try x finally x
}

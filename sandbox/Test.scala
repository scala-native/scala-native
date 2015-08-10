package test

object M extends C {
  println(foo())
  object Nested {
    println(foo())
  }
}

trait J extends I

trait I {
  def foo(): Int
}

class C extends I {
  def foo(): Int = 3
}

package test

class Test {
  def id[T](t: T): T = t
  def foo: Int = id(1)
}

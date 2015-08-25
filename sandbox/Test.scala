package test

class A {
  def foo = 42
}

class B extends A {
  override def foo =
    try throw new Exception
    finally {
      println(42)
    }
}

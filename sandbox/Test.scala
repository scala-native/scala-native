package test

class A {
  def foo = 42
}

class B extends A {
  override def foo = super.foo * 2
}

package test

class A {
  def poly[T](x: T): T = x
  def foo = poly(())
}

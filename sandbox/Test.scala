package test

trait Foo {
  def foo: Int
  def bar: Int = foo + 1
}
class Bar extends Foo{
  def foo = 2
}

object Test { // 2
  def main(args: Array[String]): Unit = {
    val x: Foo = new Bar
    x.foo
    val y: Any = x
    y.isInstanceOf[Foo]
  }
}

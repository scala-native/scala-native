import scalanative.annotation.InlineSource

object Hello {
  def main(args: Array[String]): Unit = {
    val f = new Foo
    f.foo()
    Bar.bar()
  }
}

@InlineSource("c", "foo")
class Foo {
  def foo(): Int = 42
}

@InlineSource("cpp", "bar")
object Bar {
  def bar(): Int = 43
}

@InlineSource("m", "not linked")
object NotLinked {}

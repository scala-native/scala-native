package test

abstract class Foo {
  var x: Int = _
  def foo: Int
}

object Test extends Foo {
  override def foo = 2
  def main(args: Array[String]): Unit =
    ()
}

object Main {
  def main(args: Array[String]): Unit =
    Foo.bar
}

object Foo {
  @scala.scalanative.native.stub
  def bar = ???
}

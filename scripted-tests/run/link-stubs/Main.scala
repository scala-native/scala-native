object Main {
  def main(args: Array[String]): Unit =
    Foo.bar
}

object Foo {
  @scalanative.annotation.stub
  def bar = ???
}

object Main {
  def main(args: Array[String]): Unit =
    Foo.bar
}

object Foo {
  @scalanative.unsafe.stub
  def bar = ???
}

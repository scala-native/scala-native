object InitNameMangling {

  class Foo(any: Any) {
    def init(): Any = ()
  }

  new Foo(()).init()

  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}

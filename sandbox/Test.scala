object Test {
  def foo(x: Int): Unit = {
    val y = x
    println("hello native")
  }

  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    foo(1)
  }
}

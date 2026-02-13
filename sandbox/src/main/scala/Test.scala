object Test {
  def main(args: Array[String]): Unit = {
    try {
      foo()
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
  def foo(): Unit = bar()
  def bar(): Unit = baz()
  def baz(): Unit = throw new RuntimeException("stack trace test")
}

object Test {
  def test() = throw new Exception("what")
  def hello() = 25
  def foo() = {
    println("yo")
    hello()

    test()
  }
  def main(args: Array[String]): Unit = {
    println("Hello, World!")

    foo()
  }
}

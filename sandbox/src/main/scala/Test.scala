object Test {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    f()
  }

  def f() = g()

  def g() = throw new Error("test")
}

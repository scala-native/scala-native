object Test {
  def main(args: Array[String]): Unit = {
    println(minus(incr(plus(1, 2)), plus(2, 3)))
  }

  def plus(a: Int, b: Int) = a + b

  def incr(x: Int) = x + 1

  def minus(a: Int, b: Int) = throw new Error("test")
}

package test

object Test {
  def f: Int = throw new Exception()
  def g: Int = {
    try {
      f
      1
    } catch {
      case _: Exception =>
        2
    }
  }
  def main(args: Array[String]): Unit = g
}

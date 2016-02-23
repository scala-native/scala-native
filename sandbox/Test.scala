package test

object Test {
  def f = throw new Exception()
  def main(args: Array[String]): Unit = {
    try f
    catch {
      case _: Exception =>
        ()
    }
  }
}

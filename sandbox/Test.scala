package test

object Test {
  def f: Int = throw new Exception()
  def g: Int = {
    try { f; 1 }
    catch {
      case _: Exception =>
        try { f; 2 }
        catch {
          case _: Exception => 3
        }
    }
  }
  def main(args: Array[String]): Unit = g
}

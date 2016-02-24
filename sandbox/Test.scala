package test

object Test {
  def g: Int = 10
  def f(i: Int) = i + 1
  def main(args: Array[String]): Unit =
    f(f(g match {
      case 0 => 1
      case 1 => 2
      case _ => 3
    }))
}

package test

object _42 extends Exception

object Test {
  def main(args: Array[String]): Unit =
    try {
      throw _42
    } catch {
      case `_42` =>
    }
}

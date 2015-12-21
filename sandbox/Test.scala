package test

object Test {
  def f(x: Int, y: Int) = {
    x match {
      case 1 => if (y > 0) y else x
      case 2 => 3
      case _ => 4
    }
  }
}

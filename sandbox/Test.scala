package test

class C {
  def foo(x: Int, y: Int): Int =
    x match {
      case 0 => x - y
      case 1 => x + y
      case 2 => x * y
      case 3 => x / y
      case _ => 0
    }
}

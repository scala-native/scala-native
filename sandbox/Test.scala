package test

class A {
  def foo = {
    val x = 1 match {
      case 0 => 1
      case 1 => 2
    }
    (x + 2) * (x + 3)
  }
}

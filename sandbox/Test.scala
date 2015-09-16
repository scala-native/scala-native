package test

class Test {
  def foo(x: Int) = {
    var y = 0
    x match {
      case 1|2|3 =>
        y = 1
        123
      case 4 | 5 =>
        y = 2
        45
      case _ => 1
    }
  }
}

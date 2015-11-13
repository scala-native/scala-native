package test

class C {
  var x = 2
}

object Test {
  def bar(x: Int) = x
  def main(args: Array[String]) = {
    val x = 1
    val y = 2
    bar(x + y)
    bar(x - y)
    bar(x * y)
    bar(x / y)
    bar(x % y)
    ()
  }
}

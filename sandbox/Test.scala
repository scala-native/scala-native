package test

class C {
  var x = 2
}

object Test {
  def bar(x: Int) = x
  def main(args: Array[String]) = {
    val c = new C
    bar(c.x + 2)
    ()
  }
}

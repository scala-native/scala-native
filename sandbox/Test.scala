package test

class C {
  var x = 2
  def foo = {
    x = 3
    x
  }
}

object Test {
  def main(args: Array[String]) = {
    val c = new C
    c.foo + 2
    ()
  }
}

package test

class C {
  var x = 2
}

object Test {
  def main(args: Array[String]) = {
    val c = new C
    c.x + 2
    ()
  }
}

import scalanative.native._
import scalanative.libc.stdlib._
import scalanative.runtime.GC

class A {
  def whoAmI() = c"A"

  def sayHello(): Unit = {
    val who = whoAmI()

    fprintf(stdout, c"%s say hello\n", who)
  }
}

class B extends A {
  override def whoAmI() = c"B"
}

object Test {
  def main (args : Array [String]) = {
    val a = new A
    a.sayHello()

    val b = new B
    b.sayHello()
  }
}

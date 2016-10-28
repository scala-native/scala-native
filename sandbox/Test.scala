import scalanative.native._, stdlib._, stdio._

object Test {
  def main(args: Array[String]): Unit = {
    val x: { def fooo(str: String): Unit } = new F();
    x.fooo(" dyn")
    new F().fooo(" non-dyn")
  }

  class F {

    def fooo(str: String) = println("hi " + str)

  }
}

import scalanative.native._, stdlib._, stdio._

object Test {
  def main(args: Array[String]): Unit = {
    val x: {
      def bar(str: String): Unit; def fooo(str1: String, str2: String): Unit
    } = new F();
    x.bar(" dyn")
    x.fooo("sakjbd", "ajhs")
  }

  class F {

    def bar(str: String) = println("hi " + str)

    def fooo(str1: String, str2: String) = println(str1 + str2)

  }
}

import scalanative.unsafe._

object Main {
  def main(args: Array[String]): Unit =
    println(s"The answer is ${Util.forty_two().toInt}")
}

@link("link-order-test")
@extern
object Util {
  def forty_two(): CInt = extern
}

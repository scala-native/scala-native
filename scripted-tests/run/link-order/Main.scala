import scalanative.native

object Main {
  def main(args: Array[String]): Unit =
    println(s"The answer is ${Util.forty_two().toInt}")
}

@native.link("link-order-test")
@native.extern
object Util {
  def forty_two(): native.CInt = native.extern
}

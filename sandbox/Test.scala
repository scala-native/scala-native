import scalanative.native._

object Test  {
  def main(args: Array[String]): Unit = {
    val u42 = 42.toUByte
    val buf = stackalloc[UByte]
    !buf = 42.toUByte
    stdio.printf(c"%d\n", u42)
  }
}

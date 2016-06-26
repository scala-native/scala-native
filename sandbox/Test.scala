import scalanative.native._, stdlib._, stdio._

object Test {
  def main(args: Array[String]): Unit = {
    val str = "abcd"
    printf(c"%d\n", str.charAt(0).toLong)
    printf(c"%d\n", str.charAt(1).toLong)
    printf(c"%d\n", str.charAt(2).toLong)
    printf(c"%d\n", str.charAt(3).toLong)
  }
}

import scalanative.native._, stdlib._, stdio._

object Test {
  def main(args: Array[String]): Unit = {
    val W = 800
    val H = 600
    fprintf(stdout, c"P3\n%d %d\n%d\n", W, H, 255)
  }
}

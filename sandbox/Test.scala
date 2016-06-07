import scalanative.native._
import scalanative.libc.stdlib._

object Test {
  def main(args: Array[String]): Unit = {
    val s = "hello"
    fprintf(stdout, c"s.length: %d", s.length)
    fprintf(stdout, c"s(0): %d", s.charAt(0).toInt)
  }
}

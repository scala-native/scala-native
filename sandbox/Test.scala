import scalanative.native._
import scalanative.libc.stdlib._

object Test {
  def main(args: Array[String]): Unit =
    fprintf(stdout, c"hello, native!")
}

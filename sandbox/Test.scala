import scalanative.native._
import scalanative.libc.stdlib._
import scalanative.runtime.GC

object Main {
  def main(args: Array[String]): Unit = {
    val (a, b) = (1, 2)
    fprintf(stdout, c"(%d, %d)", a, b)
  }
}

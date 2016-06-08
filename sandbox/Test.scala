import scalanative.native._
import scalanative.libc.stdlib._
import scalanative.runtime.GC

object Test {
  def main(args: Array[String]): Unit = {
    val l = 43.toUInt
    val r = 2.toUInt
    fprintf(stdout, c"43 div 2 == %d\n", (l / r).toInt)
    fprintf(stdout, c"43 rem 2 == %d\n", (l % r).toInt)
  }
}

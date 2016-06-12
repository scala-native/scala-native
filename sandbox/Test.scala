import scalanative.native._
import scalanative.libc.stdlib._

object Test {
  def main(args: Array[String]): Unit = {
    val list = List(1, 2, 3)
    val sum = list.sum
    fprintf(stdout, c"sum is %d\n", sum)
  }
}

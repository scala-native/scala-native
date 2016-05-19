import scalanative.native._
import scalanative.libc.stdlib._

object Test {
  def main(args: Array[String]): Unit = {
    val ptr = malloc(sizeof[Int]).cast[Ptr[Int]]
    ptr(0) = 42
    fprintf(__stdoutp, c"%d\n", ptr(0))
  }
}

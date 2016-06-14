import scalanative.native._
import scalanative.libc.stdlib._

object Test {
  def main(args: Array[String]): Unit = {
    val p = (n: CString) => {
      fprintf(stderr, c"Hello %s!\n", n)
    }
    p(c"World")
  }
}

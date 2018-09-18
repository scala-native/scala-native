import scala.scalanative.native._
import scala.scalanative.libc.stdio._

object Hello {
  def main(args: Array[String]): Unit = {
    fprintf(stderr, c"Hello, world!")
  }
}

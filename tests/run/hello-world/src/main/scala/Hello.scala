import scala.scalanative.native._, stdio._

object Hello {
  def main(args: Array[String]): Unit = {
    fprintf(stderr, c"Hello, world!")
  }
}

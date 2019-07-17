import scala.scalanative.unsafe._
import scala.scalanative.libc.stdio._

object Hello {
  def main(args: Array[String]): Unit = {
    Zone { implicit z =>
      vfprintf(stderr, c"Hello, world!", toCVarArgList())
    }
  }
}

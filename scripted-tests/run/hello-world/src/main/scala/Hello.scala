import scala.scalanative.unsafe._
import scala.scalanative.libc.stdio._

object Hello {
  def main(args: Array[String]): Unit = {
    Zone.acquire { implicit z =>
      vfprintf(stderr, c"Hello, world!", toCVarArgList())
    }
  }
}

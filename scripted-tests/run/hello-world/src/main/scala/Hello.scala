import scala.scalanative.libc.stdio._
import scala.scalanative.unsafe._

object Hello {
  def main(args: Array[String]): Unit = {
    Zone.acquire { implicit z =>
      vfprintf(stderr, c"Hello, world!", toCVarArgList())
    }
  }
}

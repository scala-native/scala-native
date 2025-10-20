import scala.scalanative.unsafe.*
import scala.scalanative.libc.stdio.*

object Hello {
  def main(args: Array[String]): Unit = {
    Zone.acquire { implicit z =>
      vfprintf(stderr, c"Hello, world!", toCVarArgList())
    }
  }
}

import scalanative.unsafe._
import scalanative.unsigned._

object Base {
  def foo(n: Int): Unit = {
    val _ = stackalloc[Byte](n.toUInt)
  }
}

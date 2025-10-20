import scalanative.unsafe.*
import scalanative.unsigned.*

object Base {
  def foo(n: Int): Unit = {
    val _ = stackalloc[Byte](n.toUInt)
  }
}

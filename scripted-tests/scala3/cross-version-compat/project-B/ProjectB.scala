import scalanative.unsafe.*
import scalanative.unsigned.*

object ProjectB {
  def foo(n: Int): Unit = {
    Base.foo(n)
    val _ = stackalloc[Byte](n.toUInt)
  }
}

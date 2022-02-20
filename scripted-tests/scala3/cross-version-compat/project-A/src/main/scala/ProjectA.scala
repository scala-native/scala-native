import scalanative.unsafe._
import scalanative.unsigned._

object ProjectA {
  def foo(n: Int): Unit = {
    Base.foo(n)
    val _ = stackalloc[Byte](n.toUInt)
  }
}

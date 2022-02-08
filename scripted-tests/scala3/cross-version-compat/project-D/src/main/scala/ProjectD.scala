import scalanative.unsafe._
import scalanative.unsigned._

object ProjectD {
  def foo(n: Int): Unit = {
    Base.foo(n)
    ProjectA.foo(n)
    ProjectB.foo(n)
    ProjectC.foo(n)
    val _ = stackalloc[Byte](n.toUInt)
  }
}

import scalanative.unsafe.*
import scalanative.unsigned.*

object ProjectF {
  def foo(n: Int): Unit = {
    Base.foo(n)
    ProjectA.foo(n)
    ProjectB.foo(n)
    ProjectC.foo(n)
    val _ = stackalloc[Byte](n.toUInt)
  }
}

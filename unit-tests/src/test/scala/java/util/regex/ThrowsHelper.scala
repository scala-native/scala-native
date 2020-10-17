package java.util.regex

import scala.scalanative.junit.utils.AssertThrows._

object ThrowsHelper {
  def assertThrowsAnd[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U)(cond: T => Boolean): Unit = {
    val c = cond(expectThrows(expectedThrowable, code))
    assert(c)
  }
}

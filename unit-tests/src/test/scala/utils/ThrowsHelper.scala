package scala.scalanative.junit.utils

import AssertThrows.assertThrows

// Calls to this should probably be changed to assertThrows.
// This was added as it was all over the place in the pre
// JUnit code.
object ThrowsHelper {
  def assertThrowsAnd[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U
  )(cond: T => Boolean): Unit = {
    val c = cond(assertThrows(expectedThrowable, code))
    assert(c)
  }
}

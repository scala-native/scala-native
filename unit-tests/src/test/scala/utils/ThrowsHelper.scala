package scala.scalanative.junit.utils

import AssertThrows._
import org.junit.Assert.assertEquals

// Calls to this should probably be changed to expectThrows.
// This was added as it was all over the place in the pre
// JUnit code.
object ThrowsHelper {
  def assertThrowsAnd[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U)(cond: T => Boolean): Unit = {
    val c = cond(expectThrows(expectedThrowable, code))
    assert(c)
  }

  def assertThrowsAndMessage[T <: Throwable, U](
      expectedThrowable: Class[T],
      code: => U)(expectedMsg: String): Unit = {
    val exception = expectThrows(expectedThrowable, code)
    assertEquals(expectedMsg, exception.toString)
  }
}

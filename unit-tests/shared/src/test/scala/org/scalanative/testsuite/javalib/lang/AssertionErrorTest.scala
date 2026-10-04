package org.scalanative.testsuite.javalib.lang

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class AssertionErrorTest {
  @Test def messageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val error = new AssertionError("assertion message", cause)
    assertEquals("assertion message", error.getMessage())
    assertSame(cause, error.getCause())
    assertThrows(
      classOf[IllegalStateException],
      error.initCause(new Throwable())
    )
  }

  @Test def nullMessageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val nullMessage = new AssertionError(null, cause)
    assertNull(nullMessage.getMessage())
    assertSame(cause, nullMessage.getCause())

    val nullCause = new AssertionError("assertion message", null)
    assertEquals("assertion message", nullCause.getMessage())
    assertNull(nullCause.getCause())

    val bothNull = new AssertionError(null, null)
    assertNull(bothNull.getMessage())
    assertNull(bothNull.getCause())
  }

  @Test def existingConstructors(): Unit = {
    val empty = new AssertionError()
    assertNull(empty.getMessage())
    assertNull(empty.getCause())
    assertEquals("detail", new AssertionError("detail").getMessage())
    assertEquals("true", new AssertionError(true).getMessage())
    assertEquals("c", new AssertionError('c').getMessage())
    assertEquals("42", new AssertionError(42).getMessage())
    assertEquals("42", new AssertionError(42L).getMessage())
    assertEquals("1.5", new AssertionError(1.5f).getMessage())
    assertEquals("1.5", new AssertionError(1.5).getMessage())
  }
}

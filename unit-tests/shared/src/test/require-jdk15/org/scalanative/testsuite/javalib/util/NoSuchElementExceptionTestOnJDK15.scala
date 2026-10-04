package org.scalanative.testsuite.javalib.util

import java.util.NoSuchElementException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class NoSuchElementExceptionTestOnJDK15 {
  @Test def messageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val exception = new NoSuchElementException("missing element", cause)
    assertEquals("missing element", exception.getMessage())
    assertSame(cause, exception.getCause())
    assertThrows(
      classOf[IllegalStateException],
      exception.initCause(new Throwable())
    )
  }

  @Test def causeOnly(): Unit = {
    val cause = new Throwable("cause message") {
      override def toString(): String = "rendered cause"
    }
    val exception = new NoSuchElementException(cause)
    assertEquals("rendered cause", exception.getMessage())
    assertSame(cause, exception.getCause())
  }

  @Test def nullMessageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val nullMessage = new NoSuchElementException(null, cause)
    assertNull(nullMessage.getMessage())
    assertSame(cause, nullMessage.getCause())

    val nullCause = new NoSuchElementException("missing element", null)
    assertEquals("missing element", nullCause.getMessage())
    assertNull(nullCause.getCause())

    val bothNull = new NoSuchElementException(null, null)
    assertNull(bothNull.getMessage())
    assertNull(bothNull.getCause())

    val causeOnly = new NoSuchElementException(null: Throwable)
    assertNull(causeOnly.getMessage())
    assertNull(causeOnly.getCause())
  }

  @Test def existingConstructors(): Unit = {
    val cause = new Throwable()
    for (exception <- Seq(
          new NoSuchElementException(),
          new NoSuchElementException(null: String),
          new NoSuchElementException("missing element")
        )) {
      assertNull(exception.getCause())
      assertSame(exception, exception.initCause(cause))
      assertSame(cause, exception.getCause())
    }
    assertNull(new NoSuchElementException().getMessage())
    assertNull(new NoSuchElementException(null: String).getMessage())
    assertEquals(
      "missing element",
      new NoSuchElementException("missing element").getMessage()
    )
  }
}

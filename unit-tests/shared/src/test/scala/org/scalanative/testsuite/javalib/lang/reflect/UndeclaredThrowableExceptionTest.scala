package org.scalanative.testsuite.javalib.lang.reflect

import java.lang.reflect.UndeclaredThrowableException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class UndeclaredThrowableExceptionTest {
  @Test def throwableOnly(): Unit = {
    val cause = new Throwable("cause message")
    val exception = new UndeclaredThrowableException(cause)
    assertNull(exception.getMessage())
    assertSame(cause, exception.getUndeclaredThrowable())
    assertSame(cause, exception.getCause())
  }

  @Test def throwableAndMessage(): Unit = {
    val cause = new Throwable("cause message")
    val exception = new UndeclaredThrowableException(cause, "wrapper message")
    assertEquals("wrapper message", exception.getMessage())
    assertSame(cause, exception.getUndeclaredThrowable())
    assertSame(cause, exception.getCause())
  }

  @Test def nullThrowableAndMessage(): Unit = {
    for (exception <- Seq(
          new UndeclaredThrowableException(null),
          new UndeclaredThrowableException(null, null),
          new UndeclaredThrowableException(null, "wrapper message")
        )) {
      assertNull(exception.getUndeclaredThrowable())
      assertNull(exception.getCause())
    }
    assertNull(new UndeclaredThrowableException(null).getMessage())
    assertNull(new UndeclaredThrowableException(null, null).getMessage())
    assertEquals(
      "wrapper message",
      new UndeclaredThrowableException(null, "wrapper message").getMessage()
    )
  }

  @Test def wrappedThrowableIsIndependentOfOverriddenCause(): Unit = {
    val cause = new Throwable("cause")
    val other = new Throwable("other")
    val exception = new UndeclaredThrowableException(cause) {
      override def getCause(): Throwable = other
    }
    assertSame(other, exception.getCause())
    assertSame(cause, exception.getUndeclaredThrowable())
  }

  @Test def causeCannotBeReplaced(): Unit = {
    val cause = new Throwable("cause")
    for (exception <- Seq(
          new UndeclaredThrowableException(cause),
          new UndeclaredThrowableException(cause, "wrapper message"),
          new UndeclaredThrowableException(null),
          new UndeclaredThrowableException(null, "wrapper message")
        )) {
      val original = exception.getCause()
      for (replacement <- Seq(new Throwable(), null, exception)) {
        assertThrows(
          classOf[IllegalStateException],
          exception.initCause(replacement)
        )
        assertSame(original, exception.getCause())
        assertSame(original, exception.getUndeclaredThrowable())
      }
    }
  }
}

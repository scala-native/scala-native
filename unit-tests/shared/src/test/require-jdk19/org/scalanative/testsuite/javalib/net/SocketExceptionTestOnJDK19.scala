package org.scalanative.testsuite.javalib.net

import java.net.SocketException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class SocketExceptionTestOnJDK19 {
  @Test def messageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val exception = new SocketException("socket message", cause)
    assertEquals("socket message", exception.getMessage())
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
    val exception = new SocketException(cause)
    assertEquals("rendered cause", exception.getMessage())
    assertSame(cause, exception.getCause())
  }

  @Test def nullMessageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val nullMessage = new SocketException(null, cause)
    assertNull(nullMessage.getMessage())
    assertSame(cause, nullMessage.getCause())

    val nullCause = new SocketException("socket message", null)
    assertEquals("socket message", nullCause.getMessage())
    assertNull(nullCause.getCause())

    val bothNull = new SocketException(null, null)
    assertNull(bothNull.getMessage())
    assertNull(bothNull.getCause())

    val causeOnly = new SocketException(null: Throwable)
    assertNull(causeOnly.getMessage())
    assertNull(causeOnly.getCause())
  }

  @Test def existingConstructors(): Unit = {
    val cause = new Throwable()
    for (exception <- Seq(
          new SocketException(),
          new SocketException(null: String),
          new SocketException("socket message")
        )) {
      assertNull(exception.getCause())
      assertSame(exception, exception.initCause(cause))
      assertSame(cause, exception.getCause())
    }
    assertNull(new SocketException().getMessage())
    assertNull(new SocketException(null: String).getMessage())
    assertEquals(
      "socket message",
      new SocketException("socket message").getMessage()
    )
  }
}

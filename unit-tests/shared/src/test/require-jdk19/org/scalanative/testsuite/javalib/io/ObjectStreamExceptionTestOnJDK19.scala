package org.scalanative.testsuite.javalib.io

import java.io.ObjectStreamException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ObjectStreamExceptionTestOnJDK19 {
  @Test def messageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val exception = new ObjectStreamException("stream message", cause) {}
    assertEquals("stream message", exception.getMessage())
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
    val exception = new ObjectStreamException(cause) {}
    assertEquals("rendered cause", exception.getMessage())
    assertSame(cause, exception.getCause())
  }

  @Test def nullMessageAndCause(): Unit = {
    val cause = new Throwable("cause message")
    val nullMessage = new ObjectStreamException(null, cause) {}
    assertNull(nullMessage.getMessage())
    assertSame(cause, nullMessage.getCause())

    val nullCause = new ObjectStreamException("stream message", null) {}
    assertEquals("stream message", nullCause.getMessage())
    assertNull(nullCause.getCause())

    val bothNull = new ObjectStreamException(null, null) {}
    assertNull(bothNull.getMessage())
    assertNull(bothNull.getCause())

    val causeOnly = new ObjectStreamException(null: Throwable) {}
    assertNull(causeOnly.getMessage())
    assertNull(causeOnly.getCause())
  }

  @Test def existingConstructors(): Unit = {
    val cause = new Throwable()
    for (exception <- Seq(
          new ObjectStreamException() {},
          new ObjectStreamException(null: String) {},
          new ObjectStreamException("stream message") {}
        )) {
      assertNull(exception.getCause())
      assertSame(exception, exception.initCause(cause))
      assertSame(cause, exception.getCause())
    }
    assertNull(new ObjectStreamException() {}.getMessage())
    assertNull(new ObjectStreamException(null: String) {}.getMessage())
    assertEquals(
      "stream message",
      new ObjectStreamException("stream message") {}.getMessage()
    )
  }
}

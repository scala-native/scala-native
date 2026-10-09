package org.scalanative.testsuite.javalib.lang.reflect

import java.lang.reflect.InvocationTargetException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InvocationTargetExceptionTest {
  @Test def targetOnly(): Unit = {
    val target = new Throwable("target message")
    val exception = new InvocationTargetException(target)
    assertNull(exception.getMessage())
    assertSame(target, exception.getTargetException())
    assertSame(target, exception.getCause())
  }

  @Test def targetAndMessage(): Unit = {
    val target = new Throwable("target message")
    val exception = new InvocationTargetException(target, "wrapper message")
    assertEquals("wrapper message", exception.getMessage())
    assertSame(target, exception.getTargetException())
    assertSame(target, exception.getCause())
  }

  @Test def nullTargetAndMessage(): Unit = {
    for (exception <- Seq(
          new InvocationTargetException(null),
          new InvocationTargetException(null, null),
          new InvocationTargetException(null, "wrapper message")
        )) {
      assertNull(exception.getTargetException())
      assertNull(exception.getCause())
    }
    assertNull(new InvocationTargetException(null).getMessage())
    assertNull(new InvocationTargetException(null, null).getMessage())
    assertEquals(
      "wrapper message",
      new InvocationTargetException(null, "wrapper message").getMessage()
    )
  }

  @Test def protectedConstructor(): Unit = {
    val exception = new InvocationTargetException() {}
    assertNull(exception.getMessage())
    assertNull(exception.getTargetException())
    assertNull(exception.getCause())
  }

  @Test def targetIsIndependentOfOverriddenCause(): Unit = {
    val target = new Throwable("target")
    val other = new Throwable("other")
    val exception = new InvocationTargetException(target) {
      override def getCause(): Throwable = other
    }
    assertSame(other, exception.getCause())
    assertSame(target, exception.getTargetException())
  }

  @Test def causeCannotBeReplaced(): Unit = {
    val target = new Throwable("target")
    for (exception <- Seq(
          new InvocationTargetException(target),
          new InvocationTargetException(target, "wrapper message"),
          new InvocationTargetException(null),
          new InvocationTargetException(null, "wrapper message"),
          new InvocationTargetException() {}
        )) {
      val original = exception.getCause()
      for (replacement <- Seq(new Throwable(), null, exception)) {
        assertThrows(
          classOf[IllegalStateException],
          exception.initCause(replacement)
        )
        assertSame(original, exception.getCause())
        assertSame(original, exception.getTargetException())
      }
    }
  }
}

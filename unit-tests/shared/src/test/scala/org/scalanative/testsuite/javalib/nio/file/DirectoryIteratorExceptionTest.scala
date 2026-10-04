package org.scalanative.testsuite.javalib.nio.file

import java.io.IOException
import java.nio.file.DirectoryIteratorException
import java.util.ConcurrentModificationException

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class DirectoryIteratorExceptionTest {
  @Test def typedCauseAndParentDispatch(): Unit = {
    val cause = new IOException("directory error") {
      override def toString(): String = "rendered directory error"
    }
    val exception = new DirectoryIteratorException(cause)
    val typedCause: IOException = exception.getCause()
    assertSame(cause, typedCause)
    assertEquals("rendered directory error", exception.getMessage())
    assertSame(cause, (exception: Throwable).getCause())
    assertSame(cause, (exception: ConcurrentModificationException).getCause())
  }

  @Test def nullCauseRejected(): Unit = {
    assertThrows(
      classOf[NullPointerException],
      new DirectoryIteratorException(null)
    )
  }

  @Test def causeCannotBeReplaced(): Unit = {
    val cause = new IOException("directory error")
    val exception = new DirectoryIteratorException(cause)
    assertThrows(
      classOf[IllegalStateException],
      exception.initCause(new IOException())
    )
    assertSame(cause, exception.getCause())
  }
}

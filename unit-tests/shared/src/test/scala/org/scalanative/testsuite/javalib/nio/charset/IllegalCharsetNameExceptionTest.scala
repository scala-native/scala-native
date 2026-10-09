package org.scalanative.testsuite.javalib.nio.charset

import java.nio.charset.IllegalCharsetNameException

import org.junit.Assert._
import org.junit.Test

class IllegalCharsetNameExceptionTest {
  @Test def charsetNameAndMessage(): Unit = {
    val name = new String("bad charset name!")
    val exception = new IllegalCharsetNameException(name)
    assertSame(name, exception.getCharsetName())
    assertEquals(name, exception.getMessage())
    assertNull(exception.getCause())
  }

  @Test def nullCharsetName(): Unit = {
    val exception = new IllegalCharsetNameException(null)
    assertNull(exception.getCharsetName())
    assertEquals("null", exception.getMessage())
  }

  @Test def constructorPreservesNameWithoutValidation(): Unit = {
    for (name <- Seq("", "UTF-8")) {
      val exception = new IllegalCharsetNameException(name)
      assertSame(name, exception.getCharsetName())
      assertEquals(name, exception.getMessage())
    }
  }
}

package org.scalanative.testsuite.javalib.io

import java.io.{OutputStream, PrintStream}
import java.nio.charset.StandardCharsets

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class PrintStreamTestOnJDK18 {

  @Test def charset(): Unit = {
    val outStream = OutputStream.nullOutputStream()
    val expectedCharset = StandardCharsets.UTF_16LE // pick a usual one.
    val ps = new PrintStream(outStream, false, expectedCharset)

    assertEquals(
      "charset",
      expectedCharset,
      ps.charset()
    )
  }
}

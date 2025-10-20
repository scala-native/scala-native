package org.scalanative.testsuite.javalib.io

import java.io.*

import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ByteArrayOutputStreamTest {

  @Test def toStringStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException], {
        val bytes = "look about you and think".getBytes // R. Feynman
        val outStream = new ByteArrayOutputStream()
        outStream.write(bytes, 0, bytes.length)
        val unused = outStream.toString("unsupported encoding")
      }
    )
  }
}

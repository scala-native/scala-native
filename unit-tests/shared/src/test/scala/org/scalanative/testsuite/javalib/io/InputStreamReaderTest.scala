package org.scalanative.testsuite.javalib.io

import java.io._
import java.nio.charset._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamReaderTest {
  class MockInputStream extends InputStream {
    private var _closed: Boolean = false

    def isClosed: Boolean = _closed

    override def close(): Unit = _closed = true

    def read(): Int = -1
  }

  @Test def shouldThrowNpeIfNullIsPassedToConstructor(): Unit = {
    assertThrows(classOf[NullPointerException], new InputStreamReader(null))
    assertThrows(
      classOf[NullPointerException],
      new InputStreamReader(new MockInputStream, null: CharsetDecoder)
    )
    assertThrows(
      classOf[NullPointerException],
      new InputStreamReader(new MockInputStream, null: Charset)
    )
    assertThrows(
      classOf[NullPointerException],
      new InputStreamReader(new MockInputStream, null: String)
    )
  }

  @Test def inputStreamReaderInputStreamStringWithUnsupportedEnc(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new InputStreamReader(new MockInputStream, "unsupported encoding")
    )
  }

  @Test def closeClosesTheInnerStream(): Unit = {
    val in = new MockInputStream
    val reader = new InputStreamReader(in)
    reader.close()
    assertTrue(in.isClosed)
  }

  @Test def closingTwiceIsHarmless(): Unit = {
    val in = new MockInputStream
    val reader = new InputStreamReader(in)
    reader.close()
    reader.close()
  }
}

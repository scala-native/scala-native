package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Test

class BufferedReaderTest {
  class MockReader extends Reader {
    var isClosed: Boolean = false

    def close(): Unit = isClosed = true

    def read(cbuf: Array[Char], off: Int, len: Int): Int = 0
  }

  @Test def closingBufferedReaderClosesInnerReader(): Unit = {
    val inner = new MockReader
    val reader = new BufferedReader(inner)
    reader.close()
    assertTrue(inner.isClosed)
  }

  @Test def closingTwiceIsHarmless(): Unit = {
    val inner = new MockReader
    val reader = new BufferedReader(inner)
    reader.close()
    reader.close()
  }
}

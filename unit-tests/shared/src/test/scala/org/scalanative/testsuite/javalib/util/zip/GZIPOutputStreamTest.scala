package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.io.{ByteArrayOutputStream, IOException, OutputStream}
import java.util.zip._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class GZIPOutputStreamTest {

  @Test def syncFlushMakesInputAvailableBeforeClose(): Unit = {
    val out = new ByteArrayOutputStream()
    val gzip = new GZIPOutputStream(out, 512, true)
    val inflater = new Inflater(true)
    try {
      // Decode the raw deflate payload after the ten-byte gzip header.
      out.reset()
      for (text <- Seq("data: hello\n\n", "data: again\n\n")) {
        val input = text.getBytes("UTF-8")
        gzip.write(input)
        gzip.flush()
        inflater.setInput(out.toByteArray())
        val decoded = new Array[Byte](512)
        assertEquals(input.length, inflater.inflate(decoded))
        assertArrayEquals(input, decoded.take(input.length))
        assertFalse(inflater.finished())
        out.reset()
      }
    } finally {
      gzip.close()
      inflater.end()
    }
  }

  @Test def constructorOutputStream(): Unit = {
    val out = new ByteArrayOutputStream()
    val outGZIP = new TestGZIPOutputStream(out)
    assertTrue(outGZIP != null)
    assertTrue(outGZIP.getChecksum().getValue() == 0)
  }

  @Test def constructorOutputStreamInt(): Unit = {
    val out = new ByteArrayOutputStream()
    val outGZIP = new TestGZIPOutputStream(out, 100)
    assertTrue(outGZIP != null)
    assertTrue(outGZIP.getChecksum().getValue() == 0)
  }

  @Test def finish(): Unit = {
    val byteArray = Array[Byte](3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w')
    val out = new ByteArrayOutputStream()
    val outGZIP = new TestGZIPOutputStream(out)

    outGZIP.finish()
    assertThrows(classOf[IOException], outGZIP.write(byteArray, 0, 1))
  }

  @Test def writeArrayByteIntInt(): Unit = {
    val byteArray = Array[Byte](3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w')
    val out = new ByteArrayOutputStream
    val outGZIP = new TestGZIPOutputStream(out)
    outGZIP.write(byteArray, 0, 10)
    assertTrue(outGZIP.getChecksum().getValue() == 3097700292L)

    assertThrows(
      classOf[IndexOutOfBoundsException],
      outGZIP.write(byteArray, 0, 11)
    )
  }

  private class TestGZIPOutputStream(out: OutputStream, size: Int)
      extends GZIPOutputStream(out, size) {
    def this(out: OutputStream) = this(out, 512)

    def getChecksum(): Checksum =
      crc
  }
}

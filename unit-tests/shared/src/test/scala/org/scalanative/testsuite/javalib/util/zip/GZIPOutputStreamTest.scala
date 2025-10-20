package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip.*
import java.io.{ByteArrayOutputStream, IOException, OutputStream}

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class GZIPOutputStreamTest {

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

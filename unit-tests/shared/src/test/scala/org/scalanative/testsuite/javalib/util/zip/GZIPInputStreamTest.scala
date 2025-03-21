package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip._
import java.io._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class GZIPInputStreamTest {

  @Test def constructorInputStream(): Unit = {
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput))
    assertTrue(inGZIP != null)
    assertTrue(inGZIP.getChecksum().getValue() == 0)
  }

  @Test def constructorInputStreamInt(): Unit = {
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput), 200)
    assertTrue(inGZIP != null)
    assertTrue(inGZIP.getChecksum().getValue() == 0)

    assertThrows(
      classOf[IllegalArgumentException],
      new TestGZIPInputStream(new ByteArrayInputStream(gInput), 0)
    )

    assertThrows(
      classOf[IOException],
      new TestGZIPInputStream(new ByteArrayInputStream(testInput), 200)
    )
  }

  @Test def readArrayByteIntInt(): Unit = {
    val orgBuf = Array[Byte]('3', '5', '2', 'r', 'g', 'e', 'f', 'd', 'e', 'w')
    var outBuf = new Array[Byte](100)
    var result = 0
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput))

    /* It appears that JDK 23 changed so that the crc is reset to 0
     * after eos (end of string) is detected.  Previous JDKs maintained the
     * crc value in this case.
     *
     * The JDK 23 (and 24) Release Notes and the Web in general is silent
     * about this change.
     *
     * Scala Native preserves the traditional behavior, even on JDK >= 23.
     */

    /* On all platforms & versions, verify the crc after all expected
     * bytes have been read and just before the read which sets eos.
     * This shows that the read() is calculating the crc correctly
     * not leaving it zero.
     * 
     * Another test could be written to show that the calculated crc is
     * actually being used to detect errors.  An exercise for the
     * reader.
     */

    while (!inGZIP.endofInput()) {
      val nRead = inGZIP.read(outBuf, result, outBuf.length - result)
      if (nRead > -1) {
        result += nRead
        if (result > orgBuf.length) {
          fail(
            s"read too many bytes, expected: ${orgBuf.length}, read ${result}"
          )
        } else if (result == orgBuf.length) {
          assertEquals(
            "checksum",
            2074883667L /* 0x7BAC3653L */,
            inGZIP.getChecksum().getValue()
          )
        } // else read more
      }
    }

    var i = 0
    while (i < orgBuf.length) {
      assertTrue(orgBuf(i) == outBuf(i))
      i += 1
    }

    // We're at the end of the stream, so boundary check doesn't matter.
    assertTrue(inGZIP.read(outBuf, 100, 1) == -1)

    val test = new Array[Byte](507)
    i = 0
    while (i < 256) {
      test(i) = i.toByte
      i += 1
    }
    i = 256
    while (i < test.length) {
      test(i) = (256 - i).toByte
      i += 1
    }
    val bout = new ByteArrayOutputStream()
    val out = new GZIPOutputStream(bout)
    out.write(test)
    out.close()
    val comp = bout.toByteArray()
    var gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512)
    var total = 0
    while ({ result = gin2.read(test); result != -1 }) {
      total += result
    }

    assertTrue(gin2.read() == -1)
    gin2.close()
    assertTrue(test.length == total)

    gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512)
    total = 0
    while ({ result = gin2.read(new Array[Byte](200)); result != -1 }) {
      total += result
    }

    assertTrue(gin2.read() == -1)
    gin2.close()
    assertTrue(test.length == total)

    gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 516)
    total = 0
    while ({ result = gin2.read(new Array[Byte](200)); result != -1 }) {
      total += result
    }
    assertTrue(gin2.read() == -1)
    gin2.close()
    assertTrue(test.length == total)

    comp(40) = 0
    gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512)
    assertThrows(classOf[IOException], while (gin2.read(test) != -1) {})

    val baos = new ByteArrayOutputStream()
    val zipout = new GZIPOutputStream(baos)
    zipout.write(test)
    zipout.close()
    outBuf = new Array[Byte](530)
    val in = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()))
    assertThrows(
      classOf[IndexOutOfBoundsException],
      in.read(outBuf, 530, 1)
    )

    var eofReached = false
    while (!eofReached) {
      result = in.read(outBuf, 0, 5)
      if (result == -1) {
        eofReached = true
      }
    }

    /* Stream is at eos at this point, so some argument combinations which
     * would throw exceptions earlier in the stream no longer do.
     */

    assertEquals("No NPE", -1, in.read(null, 100, 1))

    assertEquals(
      "No IndexOutOfBoundsException, negative origin",
      -1,
      in.read(outBuf, -100, 1)
    )

    assertEquals(
      "No IndexOutOfBoundsException, negative length",
      -1,
      in.read(outBuf, 100, -2)
    )

    assertEquals(
      "No IndexOutOfBoundsException, negative length",
      -1,
      in.read(outBuf, 1, 2 * outBuf.length)
    )
  }

  @Test def close(): Unit = {
    val outBuf = new Array[Byte](100)
    var result = 0
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput))

    inGZIP.close()
    assertThrows(classOf[IOException], inGZIP.read())
  }

  @Test def read(): Unit = {
    var result = 0
    var buffer = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val out = new ByteArrayOutputStream()
    val gout = new GZIPOutputStream(out)

    var i = 0
    while (i < 10) {
      gout.write(buffer)
      i += 1
    }
    gout.finish()
    out.write(1)

    val gis =
      new GZIPInputStream(new ByteArrayInputStream(out.toByteArray()))
    buffer = new Array[Byte](100)
    gis.read(buffer)
    result = gis.read()
    gis.close()

    assertTrue(result == -1)
  }

  private val testInput =
    Array[Byte](9, 99, 114, 99, 46, 114, 101, 115, 101, 116, 40, 41, 59, 13, 10,
      9, 99, 114, 99, 46, 117, 112, 100, 97, 116, 101, 40, 49, 41, 59, 13, 10,
      9, 47, 47, 83, 121, 115, 116, 101, 109, 46, 111, 117, 116, 46, 112, 114,
      105, 110, 116, 40, 34, 118, 97, 108, 117, 101, 32, 111, 102, 32, 99, 114,
      99, 34, 43, 99, 114, 99, 46, 103, 101, 116, 86, 97, 108, 117, 101, 40, 41,
      41, 59, 32, 13, 10, 9)

  private val gInput =
    Array[Byte](31, -117, 8, 8, -3, 52, -77, 68, 0, 3, 104, 121, 116, 115, 95,
      103, 73, 110, 112, 117, 116, 0, 51, 54, 53, 42, 74, 79, 77, 75, 73, 45, 7,
      0, 83, 54, -84, 123, 10, 0, 0, 0)

  private class TestGZIPInputStream(in: InputStream, size: Int)
      extends GZIPInputStream(in, size) {
    def this(in: InputStream) = this(in, 512)

    def getChecksum(): Checksum =
      crc

    def endofInput(): Boolean =
      eos
  }

}

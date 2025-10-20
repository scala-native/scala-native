package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip.*
import java.io.*

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper.*

class InflaterInputStreamTest {

  var outPutBuf = new Array[Byte](500)

  private class MyInflaterInputStream(
      in: InputStream,
      infl: Inflater,
      size: Int
  ) extends InflaterInputStream(in, infl, size) {
    def this(in: InputStream, infl: Inflater) = this(in, infl, 512)
    def this(in: InputStream) = this(in, new Inflater)
    def myFill(): Unit = fill()
  }

  @Test def constructorInputStreamInflater(): Unit = {
    val byteArray = new Array[Byte](100)
    val infile = new ByteArrayInputStream(
      Array(0x9c, 0x78, 0x64, 0x63, 0x61, 0x66, 0x28, 0xe7, 0xc8, 0xc9, 0x66,
        0x62, 0x00, 0x03, 0xde, 0x05, 0x67, 0x01).map(_.toByte)
    )
    val inflate = new Inflater
    assertThrows(
      classOf[IllegalArgumentException],
      new InflaterInputStream(infile, inflate, -1)
    )
  }

  @Test def mark(): Unit = {
    val is = new ByteArrayInputStream(new Array[Byte](10))
    val iis = new InflaterInputStream(is)
    // mark do nothing, do no check
    iis.mark(0)
    iis.mark(-1)
    iis.mark(10000000)
  }

  @Test def markSupported(): Unit = {
    val is = new ByteArrayInputStream(new Array[Byte](10))
    val iis = new InflaterInputStream(is)
    assertTrue(!iis.markSupported())
    assertTrue(is.markSupported())
  }

  @Test def read(): Unit = {
    var result = 0
    var buffer = new Array[Int](5000)
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)
    val infile = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte)
    )
    val inflate = new Inflater()
    val inflatIP = new InflaterInputStream(infile, inflate)

    var i = 0
    while ({ result = inflatIP.read(); result != -1 }) {
      buffer(i) = result
      i += 1
    }
    inflatIP.close()

    var j = 0
    while (j < orgBuffer.length) {
      assertTrue(orgBuffer(j) == buffer(j))
      j += 1
    }
  }

  @Test def readArrayByteIntInt(): Unit = {
    var result = 0
    val infile = new ByteArrayInputStream(
      Array(0x9c, 0x78, 0x61, 0x66, 0x00, 0xe7, 0x00, 0x00, 0x00, 0x38, 0x00,
        0x18).map(_.toByte)
    )
    val inflate = new Inflater()
    val inflatIP = new InflaterInputStream(infile, inflate)

    val b = new Array[Byte](3)

    assertTrue(0 == inflatIP.read(b, 0, 0))

    assertThrows(classOf[IndexOutOfBoundsException], inflatIP.read(b, 5, 2))

    inflatIP.close()
    assertThrows(classOf[IOException], inflatIP.read(b, 0, 1))
  }

  @Test def availableNonEmptySource(): Unit = {
    // this byte[] is a deflation of these bytes: {1, 3, 4, 6 }
    val deflated =
      Array[Byte](72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0)
    val in = new InflaterInputStream(new ByteArrayInputStream(deflated))
    assertTrue(1 == in.read())
    assertTrue(1 == in.available())
    assertTrue(3 == in.read())
    assertTrue(1 == in.available())
    assertTrue(4 == in.read())
    assertTrue(1 == in.available())
    assertTrue(6 == in.read())
    // Depending on JDK version behaviour here may differ.
    // On Java 8 `in.available` would now return 1, since it has not yet read
    // the EOF mark. However, on JDK 15 it would return 0, which would match
    // our implementation as well as Apache Harmony.
    assumeNotJVMCompliant()
    assertTrue(0 == in.available())
    assertTrue(-1 == in.read())
    assertTrue(-1 == in.read())
  }

  @Test def availableSkip(): Unit = {
    // this byte[] is a deflation of these bytes: {1, 3, 4, 6 }
    val deflated =
      Array[Byte](72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0)
    val in = new InflaterInputStream(new ByteArrayInputStream(deflated))
    assertEquals(1, in.available())
    assertEquals(4, in.skip(4))
    // Depending on JDK version behaviour here may differ.
    // For detals see comment in availableNonEmptySource
    assumeNotJVMCompliant()
    assertEquals(0, in.available())
  }

  @Test def availableEmptySource(): Unit = {
    // this byte[] is a deflation of the empty file
    val deflated = Array[Byte](120, -100, 3, 0, 0, 0, 0, 1)
    val in = new InflaterInputStream(new ByteArrayInputStream(deflated))
    assertTrue(-1 == in.read())
    assertTrue(-1 == in.read())
    assertTrue(0 == in.available())
  }

  @Test def readArrayByteIntInt1(): Unit = {
    val test = new Array[Byte](507)
    var i = 0
    while (i < 256) {
      test(i) = i.toByte
      i += 1
    }
    while (i < test.length) {
      test(i) = (256 - i).toByte
      i += 1
    }
    val baos = new ByteArrayOutputStream()
    val dos = new DeflaterOutputStream(baos)
    dos.write(test)
    dos.close()
    val is = new ByteArrayInputStream(baos.toByteArray())
    val iis = new InflaterInputStream(is)
    val outBuf = new Array[Byte](530)
    var result = 0
    var eofReached = false
    while (!eofReached) {
      result = iis.read(outBuf, 0, 5)
      if (result == -1) {
        eofReached = true
      }
    }
    assertThrows(classOf[IndexOutOfBoundsException], iis.read(outBuf, -1, 10))
  }

  @Test def readArrayByteIntInt2(): Unit = {

    val bis = new ByteArrayInputStream(ZipBytes.brokenManifestBytes)
    val iis = new InflaterInputStream(bis)
    val outBuf = new Array[Byte](530)
    iis.close()

    // Input data is malformed
    assertThrows(classOf[IOException], iis.read(outBuf, 0, 5))
  }

  @Test def readArrayByteIntInt3(): Unit = {
    val bis = new ByteArrayInputStream(ZipBytes.brokenManifestBytes)
    val iis = new InflaterInputStream(bis)
    val outBuf = new Array[Byte](530)

    assertThrows(classOf[IOException], iis.read())
  }

  @Test def reset(): Unit = {
    val bis = new ByteArrayInputStream(new Array[Byte](10))
    val iis = new InflaterInputStream(bis)

    assertThrows(classOf[IOException], iis.reset())
  }

  @Test def skipJ(): Unit = {
    val bytes = Array(0x78, 0x9c, 0x63, 0x65, 0x61, 0x66, 0xe2, 0x60, 0x67,
      0xe0, 0x64, 0x03, 0x00, 0x00, 0xd3, 0x00, 0x2d, 0x00).map(_.toByte)
    val bis = new ByteArrayInputStream(bytes)
    val iis = new InflaterInputStream(bis)

    assertThrows(classOf[IllegalArgumentException], iis.skip(-3))
    assertTrue(iis.read() == 5)

    assertThrows(classOf[IllegalArgumentException], iis.skip(Int.MinValue))
    assertTrue(iis.read() == 4)

    assertTrue(iis.skip(3) == 3)
    assertTrue(iis.read() == 7)
    assertTrue(iis.skip(0) == 0)
    assertTrue(iis.read() == 0)

    assertTrue(iis.skip(4) == 2)
    assertTrue(iis.read() == -1)
    iis.close()
  }

  @Test def skipJ2(): Unit = {
    var result = 0
    val buffer = new Array[Int](100)
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)

    val infile = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte)
    )
    val inflate = new Inflater()
    val inflatIP = new InflaterInputStream(infile, inflate, 10)
    var skip: Long = 0L

    assertThrows(classOf[IllegalArgumentException], inflatIP.skip(Int.MinValue))
    inflatIP.close()

    val infile2 = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte)
    )
    val inflatIP2 = new InflaterInputStream(infile2)

    skip = inflatIP2.skip(Int.MaxValue)
    assertTrue(skip == 5)
    inflatIP2.close()

    val infile3 = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte)
    )
    val inflatIP3 = new InflaterInputStream(infile3)
    skip = inflatIP3.skip(2)
    assertTrue(2 == skip)

    var i = 0
    result = 0
    while ({ result = inflatIP3.read(); result != -1 }) {
      buffer(i) = result
      i += 1
    }
    inflatIP3.close()

    var j = 2
    while (j < orgBuffer.length) {
      assertTrue(orgBuffer(j) == buffer(j - 2))
      j += 1
    }
  }

  @Test def available(): Unit = {
    val bytes = Array(0x78, 0x9c, 0x63, 0x65, 0x61, 0x66, 0xe2, 0x60, 0x67,
      0xe0, 0x64, 0x03, 0x00, 0x00, 0xd3, 0x00, 0x2d, 0x00).map(_.toByte)
    val bis = new ByteArrayInputStream(bytes)
    val iis = new InflaterInputStream(bis)
    var available = 0

    var i = 0
    while (i < 11) {
      iis.read()
      available = iis.available()
      if (available == 0) {
        assertTrue(-1 == iis.read())
      } else {
        assertTrue(available == 1)
      }
      i += 1
    }
    iis.close()

    assertThrows(classOf[IOException], iis.available())
  }

  @Test def close(): Unit = {
    val iin =
      new InflaterInputStream(new ByteArrayInputStream(new Array[Byte](0)))
    iin.close()
    iin.close()
  }
}

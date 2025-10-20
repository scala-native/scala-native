package org.scalanative.testsuite.javalib.util.zip

import java.io.*
import java.util.zip.*

import org.junit.Test
import org.junit.Assert.*

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper.*

class DeflaterOutputStreamTest {

  @Test def deflaterOutputStreamCanDeflateSomeBytes(): Unit = {
    val bos = new ByteArrayOutputStream()
    val out = new DeflaterOutputStream(bos)
    val expected = Array[Byte](120, -100, 99, 100, 98, 6, 0, 0, 13, 0, 7)
    out.write(Array[Byte](1, 2, 3))
    out.close()

    val result = bos.toByteArray()

    assertTrue(result.length == expected.length)
    result.zip(expected).foreach {
      case (a, b) => assertTrue(a == b)
    }
  }

  @Test def deflaterOutputStreamCanDeflateMoreBytesThanItsBufferSize(): Unit = {
    val bos = new ByteArrayOutputStream
    val out = new DeflaterOutputStream(bos, new Deflater, 16)
    val expected = Array(120, -100, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0, 0,
      6, 120, 4, 1)
    val bytes = Array.fill[Byte](1024)(1)
    out.write(bytes)
    out.close()

    val result = bos.toByteArray()

    assertTrue(result.length == expected.length)
    result.zip(expected).foreach {
      case (a, b) => assertTrue(a == b)
    }
  }

  @Test def deflaterOutputStreamCanBeFlushedWithSyncFlush(): Unit = {
    assumeNotJVMCompliant()
    val bos = new ByteArrayOutputStream
    val out =
      new DeflaterOutputStream(bos, new Deflater, 16, /* syncFlush = */ true)
    val expected = Array(120, -100, 99, 100, 28, 5, -93, 96, 20, -116, 84, 0, 0,
      6, 120, 4, 1)
    val bytes = Array.fill[Byte](1024)(1)
    out.write(bytes)
    out.flush()
    out.close()

    val result = bos.toByteArray()

    assertTrue(result.length == expected.length)
    result.zip(expected).foreach {
      case (a, b) => assertTrue(a == b)
    }
  }

  // The following tests are
  // Ported from Apache Harmony
  private class MyDeflaterOutputStream(
      out: OutputStream,
      defl: Deflater,
      size: Int
  ) extends DeflaterOutputStream(out, defl, size) {
    def this(out: OutputStream, defl: Deflater) = this(out, defl, 512)
    def this(out: OutputStream) = this(out, new Deflater)
    var deflateFlag: Boolean = false
    def getBuf(): Array[Byte] = buf
    override protected def deflate(): Unit = {
      deflateFlag = true
      super.deflate()
    }
  }

  private var outPutBuf = new Array[Byte](500)
  private def setUp(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8)
    var x = 0
    val deflate = new Deflater(1)
    deflate.setInput(byteArray)
    while (!deflate.needsInput()) {
      x += deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    deflate.finish()
    while (!deflate.finished()) {
      x = x + deflate.deflate(outPutBuf, x, outPutBuf.length - x)
    }
    deflate.end()
  }

  @Test def constructorOutputStreamDeflater(): Unit = {
    val byteArray = Array[Byte](1, 3, 4, 7, 8)
    val f1 = File.createTempFile("hyts_constru(OD)", ".tst")
    val fos = new FileOutputStream(f1)
    val defl = new Deflater()
    val dos = new MyDeflaterOutputStream(fos, defl)

    assertTrue(dos.getBuf().length == 512)
    dos.write(byteArray)
    dos.close()
    f1.delete()
  }

  @Test def constructorOutputStreamDeflaterInt(): Unit = {
    val buf = 5
    val negBuf = -5
    val zeroBuf = 0
    val byteArray = Array[Byte](1, 3, 4, 7, 8, 3, 6)
    val f1 = File.createTempFile("gyts_Constru(ODI)", ".tst")
    val fos = new FileOutputStream(f1)
    val defl = new Deflater()

    assertThrows(
      classOf[IllegalArgumentException],
      new MyDeflaterOutputStream(fos, defl, negBuf)
    )

    assertThrows(
      classOf[IllegalArgumentException],
      new MyDeflaterOutputStream(fos, defl, zeroBuf)
    )

    val dos = new MyDeflaterOutputStream(fos, defl, buf)
    assertTrue(dos.getBuf().length == 5)
    dos.write(byteArray)
    dos.close()
    f1.delete()
  }

  @Test def deflaterOutputStreamClose(): Unit = {
    val f1 = File.createTempFile("close", ".tst")
    var iis = new InflaterInputStream(new FileInputStream(f1))
    assertThrows(classOf[EOFException], iis.read())
    iis.close()

    val fos = new FileOutputStream(f1)
    val dos = new DeflaterOutputStream(fos)
    val byteArray = Array[Byte](1, 3, 4, 6)
    dos.write(byteArray)
    dos.close()

    iis = new InflaterInputStream(new FileInputStream(f1))
    assertTrue(iis.read() == 1)
    assertTrue(iis.read() == 3)
    assertTrue(iis.read() == 4)
    assertTrue(iis.read() == 6)
    assertTrue(iis.read() == -1)
    assertTrue(iis.read() == -1)
    iis.close()

    val fos2 = new FileOutputStream(f1)
    val dos2 = new DeflaterOutputStream(fos2)
    fos2.close()
    assertThrows(classOf[IOException], dos2.close())

    assertThrows(classOf[IOException], dos.write(5))

    assertThrows(classOf[IOException], fos.write("testing".getBytes()))
    f1.delete()
  }
}

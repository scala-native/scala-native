package org.scalanative.testsuite.javalib.util.zip

import java.util.zip._
import java.io._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import scala.scalanative.junit.utils.AssumesHelper._

class InflaterOutputStreamTest {

  var outPutBuf = new Array[Byte](500)

  private class MyInflaterOutputStream(
      out: OutputStream,
      infl: Inflater,
      size: Int
  ) extends InflaterOutputStream(out, infl, size) {
    def this(out: OutputStream, infl: Inflater) = this(out, infl, 512)
    def this(out: OutputStream) = this(out, new Inflater)
  }

  @Test def constructorOutputStreamInflater(): Unit = {
    val byteArray = new Array[Byte](100)
    val outfile = new ByteArrayOutputStream(100)
    val inflate = new Inflater
    assertThrows(
      classOf[IllegalArgumentException],
      new InflaterOutputStream(outfile, inflate, -1)
    )
  }

  @Test def writeByte(): Unit = {
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)
    val infile = Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00,
      0x00, 0x38, 0x00, 0x18, 0x00).map(_.toByte)
    val inflate = new Inflater()
    val baos = new ByteArrayOutputStream(100)
    val ios = new InflaterOutputStream(baos, inflate)

    infile.foreach { b =>
      ios.write(b)
    }
    ios.close()

    assertArrayEquals(orgBuffer, baos.toByteArray())
  }

  @Test def writeByteArray(): Unit = {
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)
    val infile = Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00,
      0x00, 0x38, 0x00, 0x18, 0x00).map(_.toByte)
    val inflate = new Inflater()
    val baos = new ByteArrayOutputStream(100)
    val ios = new InflaterOutputStream(baos, inflate)

    ios.write(infile)
    ios.close()

    assertArrayEquals(orgBuffer, baos.toByteArray())
  }

  @Test def writeByteArrayRegion(): Unit = {
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)
    val infile = Array(-1, -1, -1, 0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7,
      0x00, 0x00, 0x00, 0x38, 0x00, 0x18, 0x00, -1, -1, -1).map(_.toByte)
    val inflate = new Inflater()
    val baos = new ByteArrayOutputStream(100)
    val ios = new InflaterOutputStream(baos, inflate)

    ios.write(infile, 3, infile.length - 6)
    ios.close()

    assertArrayEquals(orgBuffer, baos.toByteArray())
  }

  @Test def throwsZipExceptionForMalformed(): Unit = {
    val inflate = new Inflater()
    val baos = new ByteArrayOutputStream(100)
    val ios = new InflaterOutputStream(baos, inflate)

    assertThrows(classOf[ZipException], ios.write(ZipBytes.brokenManifestBytes))
  }

  @Test def throwsIOExceptionAfterClosed(): Unit = {
    val inflate = new Inflater()
    val baos = new ByteArrayOutputStream(100)
    val ios = new InflaterOutputStream(baos, inflate)
    ios.close()

    assertThrows(classOf[IOException], ios.write(1))
  }

}

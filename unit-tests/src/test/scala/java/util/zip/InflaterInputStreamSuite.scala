package java.util.zip

// Ported from Apache Harmony

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream,
  EOFException,
  IOException,
  InputStream,
  File,
  FileInputStream
}

object InflaterInputStreamSuite extends tests.Suite {
  var outPutBuf = new Array[Byte](500)

  private class MyInflaterInputStream(in: InputStream,
                                      infl: Inflater,
                                      size: Int)
      extends InflaterInputStream(in, infl, size) {
    def this(in: InputStream, infl: Inflater) = this(in, infl, 512)
    def this(in: InputStream) = this(in, new Inflater)
    def myFill(): Unit = fill()
  }

  test("Constructor(InputStream, Inflater)") {
    val byteArray = new Array[Byte](100)
    val infile = new ByteArrayInputStream(
      Array(0x9C, 0x78, 0x64, 0x63, 0x61, 0x66, 0x28, 0xe7, 0xc8, 0xc9, 0x66,
        0x62, 0x00, 0x03, 0xde, 0x05, 0x67, 0x01).map(_.toByte))
    val inflate = new Inflater
    assertThrows[IllegalArgumentException] {
      new InflaterInputStream(infile, inflate, -1)
    }
  }

  test("mark()") {
    val is  = new ByteArrayInputStream(new Array[Byte](10))
    val iis = new InflaterInputStream(is)
    // mark do nothing, do no check
    iis.mark(0)
    iis.mark(-1)
    iis.mark(10000000)
  }

  test("markSupported()") {
    val is  = new ByteArrayInputStream(new Array[Byte](10))
    val iis = new InflaterInputStream(is)
    assert(!iis.markSupported())
    assert(is.markSupported())
  }

  test("read()") {
    var result    = 0
    var buffer    = new Array[Int](5000)
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)
    val infile = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte))
    val inflate  = new Inflater()
    val inflatIP = new InflaterInputStream(infile, inflate)

    var i = 0
    while ({ result = inflatIP.read(); result != -1 }) {
      buffer(i) = result
      i += 1
    }
    inflatIP.close()

    var j = 0
    while (j < orgBuffer.length) {
      assert(orgBuffer(j) == buffer(j))
      j += 1
    }
  }

  test("read(Array[Byte], Int, Int)") {
    var result = 0
    val infile = new ByteArrayInputStream(
      Array(0x9c, 0x78, 0x61, 0x66, 0x00, 0xe7, 0x00, 0x00, 0x00, 0x38, 0x00,
        0x18).map(_.toByte))
    val inflate  = new Inflater()
    val inflatIP = new InflaterInputStream(infile, inflate)

    val b = new Array[Byte](3)

    assert(0 == inflatIP.read(b, 0, 0))

    assertThrows[IndexOutOfBoundsException] {
      inflatIP.read(b, 5, 2)
    }

    inflatIP.close()
    assertThrows[IOException] {
      inflatIP.read(b, 0, 1)
    }
  }

  test("AvailableNonEmptySource") {
    // this byte[] is a deflation of these bytes: {1, 3, 4, 6 }
    val deflated =
      Array[Byte](72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0)
    val in = new InflaterInputStream(new ByteArrayInputStream(deflated))
    assert(1 == in.read())
    assert(1 == in.available())
    assert(3 == in.read())
    assert(1 == in.available())
    assert(4 == in.read())
    assert(1 == in.available())
    assert(6 == in.read())
    assert(0 == in.available())
    assert(-1 == in.read())
    assert(-1 == in.read())
  }

  test("AvailableSkip") {
    // this byte[] is a deflation of these bytes: {1, 3, 4, 6 }
    val deflated =
      Array[Byte](72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0)
    val in = new InflaterInputStream(new ByteArrayInputStream(deflated))
    assert(1 == in.available())
    assert(4 == in.skip(4))
    assert(0 == in.available())
  }

  test("AvailableEmptySource") {
    // this byte[] is a deflation of the empty file
    val deflated = Array[Byte](120, -100, 3, 0, 0, 0, 0, 1)
    val in       = new InflaterInputStream(new ByteArrayInputStream(deflated))
    assert(-1 == in.read())
    assert(-1 == in.read())
    assert(0 == in.available())
  }

  test("read(Array[Byte], Int, Int)") {
    val test = new Array[Byte](507)
    var i    = 0
    while (i < 256) {
      test(i) = i.toByte
      i += 1
    }
    while (i < test.length) {
      test(i) = (256 - i).toByte
      i += 1
    }
    val baos = new ByteArrayOutputStream()
    val dos  = new DeflaterOutputStream(baos)
    dos.write(test)
    dos.close()
    val is         = new ByteArrayInputStream(baos.toByteArray())
    val iis        = new InflaterInputStream(is)
    val outBuf     = new Array[Byte](530)
    var result     = 0
    var eofReached = false
    while (!eofReached) {
      result = iis.read(outBuf, 0, 5)
      if (result == -1) {
        eofReached = true
      }
    }
    assertThrows[IndexOutOfBoundsException] {
      iis.read(outBuf, -1, 10)
    }
  }

  test("read(Array[Byte], Int, Int) 2") {

    val bis    = new ByteArrayInputStream(ZipBytes.brokenManifestBytes)
    val iis    = new InflaterInputStream(bis)
    val outBuf = new Array[Byte](530)
    iis.close()

    // Input data is malformed
    assertThrows[IOException] {
      iis.read(outBuf, 0, 5)
    }

  }

  test("read(Array[Byte], Int, Int) 3") {
    val bis    = new ByteArrayInputStream(ZipBytes.brokenManifestBytes)
    val iis    = new InflaterInputStream(bis)
    val outBuf = new Array[Byte](530)

    assertThrows[IOException] {
      iis.read()
    }
  }

  test("reset()") {
    val bis = new ByteArrayInputStream(new Array[Byte](10))
    val iis = new InflaterInputStream(bis)

    assertThrows[IOException] {
      iis.reset()
    }
  }

  test("skipJ()") {
    val bytes = Array(0x78, 0x9c, 0x63, 0x65, 0x61, 0x66, 0xe2, 0x60, 0x67,
      0xe0, 0x64, 0x03, 0x00, 0x00, 0xd3, 0x00, 0x2d, 0x00).map(_.toByte)
    val bis = new ByteArrayInputStream(bytes)
    val iis = new InflaterInputStream(bis)

    assertThrows[IllegalArgumentException] {
      iis.skip(-3)
    }
    assert(iis.read() == 5)

    assertThrows[IllegalArgumentException] {
      iis.skip(Int.MinValue)
    }
    assert(iis.read() == 4)

    assert(iis.skip(3) == 3)
    assert(iis.read() == 7)
    assert(iis.skip(0) == 0)
    assert(iis.read() == 0)

    assert(iis.skip(4) == 2)
    assert(iis.read() == -1)
    iis.close()
  }

  test("skipJ2") {
    var result    = 0
    val buffer    = new Array[Int](100)
    val orgBuffer = Array[Byte](1, 3, 4, 7, 8)

    val infile = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte))
    val inflate    = new Inflater()
    val inflatIP   = new InflaterInputStream(infile, inflate, 10)
    var skip: Long = 0L

    assertThrows[IllegalArgumentException] {
      inflatIP.skip(Int.MinValue)
    }
    inflatIP.close()

    val infile2 = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte))
    val inflatIP2 = new InflaterInputStream(infile2)

    skip = inflatIP2.skip(Int.MaxValue)
    assert(skip == 5)
    inflatIP2.close()

    val infile3 = new ByteArrayInputStream(
      Array(0x78, 0x9c, 0x63, 0x64, 0x66, 0x61, 0xe7, 0x00, 0x00, 0x00, 0x38,
        0x00, 0x18, 0x00).map(_.toByte))
    val inflatIP3 = new InflaterInputStream(infile3)
    skip = inflatIP3.skip(2)
    assert(2 == skip)

    var i = 0
    result = 0
    while ({ result = inflatIP3.read(); result != -1 }) {
      buffer(i) = result
      i += 1
    }
    inflatIP3.close()

    var j = 2
    while (j < orgBuffer.length) {
      assert(orgBuffer(j) == buffer(j - 2))
      j += 1
    }
  }

  test("available()") {
    val bytes = Array(0x78, 0x9c, 0x63, 0x65, 0x61, 0x66, 0xe2, 0x60, 0x67,
      0xe0, 0x64, 0x03, 0x00, 0x00, 0xd3, 0x00, 0x2d, 0x00).map(_.toByte)
    val bis       = new ByteArrayInputStream(bytes)
    val iis       = new InflaterInputStream(bis)
    var available = 0

    var i = 0
    while (i < 11) {
      iis.read()
      available = iis.available()
      if (available == 0) {
        assert(-1 == iis.read())
      } else {
        assert(available == 1)
      }
      i += 1
    }

    iis.close()

    assertThrows[IOException] {
      iis.available()
    }
  }

  test("close()") {
    val iin =
      new InflaterInputStream(new ByteArrayInputStream(new Array[Byte](0)))
    iin.close()
    iin.close()
  }

}

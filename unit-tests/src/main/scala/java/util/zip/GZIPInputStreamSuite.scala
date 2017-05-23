package java.util.zip

// Ported from Apache Harmony

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream,
  InputStream,
  IOException
}

object GZIPInputStreamSuite extends tests.Suite {

  test("Constructor(InputStream)") {
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput))
    assert(inGZIP != null)
    assert(inGZIP.getChecksum().getValue() == 0)
  }

  test("Constructor(InputStream, Int)") {
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput), 200)
    assert(inGZIP != null)
    assert(inGZIP.getChecksum().getValue() == 0)

    assertThrows[IllegalArgumentException] {
      new TestGZIPInputStream(new ByteArrayInputStream(gInput), 0)
    }

    assertThrows[IOException] {
      new TestGZIPInputStream(new ByteArrayInputStream(testInput), 200)
    }
  }

  test("read(Array[Byte], Int, Int)") {
    val orgBuf = Array[Byte]('3', '5', '2', 'r', 'g', 'e', 'f', 'd', 'e', 'w')
    var outBuf = new Array[Byte](100)
    var result = 0
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput))
    while (!inGZIP.endofInput()) {
      result += inGZIP.read(outBuf, result, outBuf.length - result)
    }
    assert(inGZIP.getChecksum().getValue() == 2074883667L)

    var i = 0
    while (i < orgBuf.length) {
      assert(orgBuf(i) == outBuf(i))
      i += 1
    }

    // We're at the end of the stream, so boundary check doesn't matter.
    assert(inGZIP.read(outBuf, 100, 1) == -1)

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
    val out  = new GZIPOutputStream(bout)
    out.write(test)
    out.close()
    val comp  = bout.toByteArray()
    var gin2  = new GZIPInputStream(new ByteArrayInputStream(comp), 512)
    var total = 0
    while ({ result = gin2.read(test); result != -1 }) {
      total += result
    }

    assert(gin2.read() == -1)
    gin2.close()
    assert(test.length == total)

    gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512)
    total = 0
    while ({ result = gin2.read(new Array[Byte](200)); result != -1 }) {
      total += result
    }

    assert(gin2.read() == -1)
    gin2.close()
    assert(test.length == total)

    gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 516)
    total = 0
    while ({ result = gin2.read(new Array[Byte](200)); result != -1 }) {
      total += result
    }
    assert(gin2.read() == -1)
    gin2.close()
    assert(test.length == total)

    comp(40) = 0
    gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512)
    assertThrows[IOException] {
      while (gin2.read(test) != -1) {}
    }

    val baos   = new ByteArrayOutputStream()
    val zipout = new GZIPOutputStream(baos)
    zipout.write(test)
    zipout.close()
    outBuf = new Array[Byte](530)
    val in = new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()))
    assertThrows[ArrayIndexOutOfBoundsException] {
      in.read(outBuf, 530, 1)
    }

    var eofReached = false
    while (!eofReached) {
      result = in.read(outBuf, 0, 5)
      if (result == -1) {
        eofReached = true
      }
    }

    result = -10
    result = in.read(null, 100, 1)
    result = in.read(outBuf, -100, 1)
    result = in.read(outBuf, -1, 1)
  }

  test("close()") {
    val outBuf = new Array[Byte](100)
    var result = 0
    val inGZIP = new TestGZIPInputStream(new ByteArrayInputStream(gInput))
    while (!inGZIP.endofInput()) {
      result += inGZIP.read(outBuf, result, outBuf.length - result)
    }
    assert(inGZIP.getChecksum().getValue() == 2074883667L)
    inGZIP.close()

    assertThrows[IOException] {
      inGZIP.read()
    }

    test("read()") {
      var result = 0
      var buffer = Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      val out    = new ByteArrayOutputStream()
      val gout   = new GZIPOutputStream(out)

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

      assert(result == -1)
    }
  }

  private val testInput =
    Array[Byte](9, 99, 114, 99, 46, 114, 101, 115, 101, 116, 40, 41, 59, 13,
      10, 9, 99, 114, 99, 46, 117, 112, 100, 97, 116, 101, 40, 49, 41, 59, 13,
      10, 9, 47, 47, 83, 121, 115, 116, 101, 109, 46, 111, 117, 116, 46, 112,
      114, 105, 110, 116, 40, 34, 118, 97, 108, 117, 101, 32, 111, 102, 32, 99,
      114, 99, 34, 43, 99, 114, 99, 46, 103, 101, 116, 86, 97, 108, 117, 101,
      40, 41, 41, 59, 32, 13, 10, 9)

  private val gInput =
    Array[Byte](31, -117, 8, 8, -3, 52, -77, 68, 0, 3, 104, 121, 116, 115, 95,
      103, 73, 110, 112, 117, 116, 0, 51, 54, 53, 42, 74, 79, 77, 75, 73, 45,
      7, 0, 83, 54, -84, 123, 10, 0, 0, 0)

  private class TestGZIPInputStream(in: InputStream, size: Int)
      extends GZIPInputStream(in, size) {
    def this(in: InputStream) = this(in, 512)

    def getChecksum(): Checksum =
      crc

    def endofInput(): Boolean =
      eos
  }

}

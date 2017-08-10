package java.util.zip

// Ported from Apache Harmony

import java.io.{ByteArrayOutputStream, IOException, OutputStream}

object GZIPOutputStreamSuite extends tests.Suite {

  test("Constructor(OutputStream)") {
    val out     = new ByteArrayOutputStream()
    val outGZIP = new TestGZIPOutputStream(out)
    assert(outGZIP != null)
    assert(outGZIP.getChecksum().getValue() == 0)
  }

  test("Constructor(OutputStream, Int)") {
    val out     = new ByteArrayOutputStream()
    val outGZIP = new TestGZIPOutputStream(out, 100)
    assert(outGZIP != null)
    assert(outGZIP.getChecksum().getValue() == 0)
  }

  test("finish()") {
    val byteArray = Array[Byte](3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w')
    val out       = new ByteArrayOutputStream()
    val outGZIP   = new TestGZIPOutputStream(out)

    outGZIP.finish()
    assertThrows[IOException] {
      outGZIP.write(byteArray, 0, 1)
    }
  }

  test("write(Array[Byte], Int, Int)") {
    val byteArray = Array[Byte](3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w')
    val out       = new ByteArrayOutputStream
    val outGZIP   = new TestGZIPOutputStream(out)
    outGZIP.write(byteArray, 0, 10)
    assert(outGZIP.getChecksum().getValue() == 3097700292L)

    assertThrows[ArrayIndexOutOfBoundsException] {
      outGZIP.write(byteArray, 0, 11)
    }
  }

  private class TestGZIPOutputStream(out: OutputStream, size: Int)
      extends GZIPOutputStream(out, size) {
    def this(out: OutputStream) = this(out, 512)

    def getChecksum(): Checksum =
      crc
  }
}

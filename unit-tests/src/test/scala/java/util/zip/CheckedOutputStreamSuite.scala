package java.util.zip

// Ported from Apache Harmony

import java.io.ByteArrayOutputStream

object CheckedOutputStreamSuite extends tests.Suite {

  test("Constructor()") {
    val out    = new ByteArrayOutputStream()
    val chkOut = new CheckedOutputStream(out, new CRC32())
    assert(chkOut.getChecksum().getValue() == 0)
  }

  test("getChecksum()") {
    val byteArray = Array[Byte](1, 2, 3, 'e', 'r', 't', 'g', 3, 6)
    val out       = new ByteArrayOutputStream()
    val chkOut    = new CheckedOutputStream(out, new Adler32())
    chkOut.write(byteArray(4))

    assert(chkOut.getChecksum().getValue() == 7536755)
    chkOut.getChecksum().reset()
    chkOut.write(byteArray, 5, 4)

    assert(chkOut.getChecksum().getValue() == 51708133)
  }

  test("write(Int)") {
    val byteArray = Array[Byte](1, 2, 3, 'e', 'r', 't', 'g', 3, 6)
    val out       = new ByteArrayOutputStream()
    val chkOut    = new CheckedOutputStream(out, new CRC32())
    byteArray.foreach(b => chkOut.write(b))
    assert(chkOut.getChecksum().getValue() != 0)
  }

  test("write(Array[Byte], Int, Int)") {
    val byteArray = Array[Byte](1, 2, 3, 'e', 'r', 't', 'g', 3, 6)
    val out       = new ByteArrayOutputStream()
    val chkOut    = new CheckedOutputStream(out, new CRC32())
    chkOut.write(byteArray, 4, 5)
    assert(chkOut.getChecksum().getValue != 0)
  }

}

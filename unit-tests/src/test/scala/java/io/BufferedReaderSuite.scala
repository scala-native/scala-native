package java.io

object BufferedReaderSuite extends tests.Suite {
  class MockReader extends Reader {
    var isClosed: Boolean = false

    def close(): Unit = isClosed = true

    def read(cbuf: Array[Char], off: Int, len: Int): Int = 0
  }

  test("Closing a `BufferedReader` closes its inner reader") {
    val inner  = new MockReader
    val reader = new BufferedReader(inner)
    reader.close()
    assert(inner.isClosed)
  }

  test("Closing twice is harmless") {
    val inner  = new MockReader
    val reader = new BufferedReader(inner)
    reader.close()
    reader.close()
  }
}

package java.io

object InputStreamReaderSuite extends tests.Suite {
  class MockInputStream extends InputStream {
    private[this] var _closed: Boolean = false

    def isClosed: Boolean = _closed

    override def close(): Unit = _closed = true

    def read(): Int = -1
  }

  test("closing closes the inner stream") {
    val in     = new MockInputStream
    val reader = new InputStreamReader(in)
    reader.close()
    assert(in.isClosed)
  }

  test("closing twice is harmless") {
    val in     = new MockInputStream
    val reader = new InputStreamReader(in)
    reader.close()
    reader.close()
  }
}

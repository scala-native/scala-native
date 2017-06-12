package java.io

object InputStreamReaderSuite extends tests.Suite {
  def withTempFile(proc: File => Unit): Unit = {
    val file = File.createTempFile("scala-native-tests", null)
    try { proc(file) } finally { file.delete() }
  }

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

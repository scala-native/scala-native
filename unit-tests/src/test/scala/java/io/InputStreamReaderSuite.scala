package java.io

import java.nio.charset._

object InputStreamReaderSuite extends tests.Suite {
  class MockInputStream extends InputStream {
    private[this] var _closed: Boolean = false

    def isClosed: Boolean = _closed

    override def close(): Unit = _closed = true

    def read(): Int = -1
  }

  test("should throw a NPE if null is passed to constructor") {
    assertThrows[NullPointerException] {
      new InputStreamReader(null)
    }
    assertThrows[NullPointerException] {
      new InputStreamReader(new MockInputStream, null: CharsetDecoder)
    }
    assertThrows[NullPointerException] {
      new InputStreamReader(new MockInputStream, null: Charset)
    }
    assertThrows[NullPointerException] {
      new InputStreamReader(new MockInputStream, null: String)
    }
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

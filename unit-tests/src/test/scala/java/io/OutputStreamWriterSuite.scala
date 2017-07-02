package java.io

import java.nio.charset._

object OutputStreamWriterSuite extends tests.Suite {
  class MockOutputStream extends OutputStream {
    override def write(b: Int): Unit = ()
  }

  test("should throw a NPE if null is passed to constructor") {
    assertThrows[NullPointerException] {
      new OutputStreamWriter(null)
    }
    assertThrows[NullPointerException] {
      new OutputStreamWriter(new MockOutputStream, null: Charset)
    }
    assertThrows[NullPointerException] {
      new OutputStreamWriter(new MockOutputStream, null: CharsetEncoder)
    }
    assertThrows[NullPointerException] {
      new OutputStreamWriter(new MockOutputStream, null: String)
    }
  }
}

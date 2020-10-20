package java.io

import java.nio.charset._

import org.junit.Test

import scalanative.junit.utils.AssertThrows._

class OutputStreamWriterTest {
  class MockOutputStream extends OutputStream {
    override def write(b: Int): Unit = ()
  }

  @Test def shouldThrowNpeIfNullPassedToConstructor(): Unit = {
    assertThrows(classOf[NullPointerException], new OutputStreamWriter(null))
    assertThrows(classOf[NullPointerException],
                 new OutputStreamWriter(new MockOutputStream, null: Charset))
    assertThrows(
      classOf[NullPointerException],
      new OutputStreamWriter(new MockOutputStream, null: CharsetEncoder))
    assertThrows(classOf[NullPointerException],
                 new OutputStreamWriter(new MockOutputStream, null: String))
  }

  @Test def outputStreamWriterOutputStreamStringWithUnsupportedEnc(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new OutputStreamWriter(new MockOutputStream, "unsupported encoding"))
  }

}

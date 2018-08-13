package java.io

import java.nio.charset._

object ByteArrayOutputStreamSuite extends tests.Suite {

  test("toString(String) with unsupported encoding") {
    assertThrows[java.io.UnsupportedEncodingException] {
      val bytes     = "look about you and think".getBytes // R. Feynman
      val outStream = new ByteArrayOutputStream()
      outStream.write(bytes, 0, bytes.length)
      val unused = outStream.toString("unsupported encoding")
    }
  }
}

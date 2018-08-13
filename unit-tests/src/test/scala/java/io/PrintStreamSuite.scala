package java.io

import java.nio.charset._

object PrintStreamSuite extends tests.Suite {

  test("PrintStream(OutputStream, String) with unsupported encoding") {
    assertThrows[java.io.UnsupportedEncodingException] {
      new PrintStream(new File("/dev/null"), "unsupported encoding")
    }
  }

  // The careful reader would expect to see tests for the constructors
  // PrintStream(String, String) and PrintStream(String, String) here.
  //
  // See the comments in PrintStream.scala for a discussion about
  // the those constructors.
  //
  // They are minimally implemented and will not link, so they can not
  // be tested here.

}

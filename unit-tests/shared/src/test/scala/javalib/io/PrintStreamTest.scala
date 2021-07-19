package javalib.io

import java.io._

import org.junit.Test

import scalanative.junit.utils.AssertThrows.assertThrows

class PrintStreamTest {

  @Test def printStreamOutputStreamStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new PrintStream(new File("/dev/null"), "unsupported encoding")
    )
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

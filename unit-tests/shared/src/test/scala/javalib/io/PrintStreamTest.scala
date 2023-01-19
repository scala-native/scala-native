package javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform.isWindows

class PrintStreamTest {

  @Test def printStreamOutputStreamStringWithUnsupportedEncoding(): Unit = {
    // Make sure to not use /dev/null on Windows leading to FileNotFoundException
    // On JVM charset check happens before file exists checks
    val devNull = if (isWindows) "NUL" else "/dev/null"
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new PrintStream(new File(devNull), "unsupported encoding")
    )
  }

  @Test def writeCharSequenceTest(): Unit = {

    val baos = new ByteArrayOutputStream()
    val ps = new PrintStream(baos)

    // Use writer with as String casted as a CharSequence
    val stringAsCharSeq: CharSequence = "ABC"
    ps.print(stringAsCharSeq)

    // Use writer with as StringBuilder casted as a CharSequence
    val stingBuilderAsCharSeq = new StringBuilder("DEF")
    ps.print(stingBuilderAsCharSeq)

    // flush is optional here, as Writer calls the no-op Outputstream.flush()
    ps.flush()

    val result = baos.toString()

    assertEquals("ABCDEF", result)
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

package org.scalanative.testsuite.javalib.io

import java.io.{File, PrintStream}
import java.nio.charset.StandardCharsets

import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows
import org.scalanative.testsuite.utils.Platform

class PrintStreamTest {

  final val secondComing = """
               |""The darkness drops again; but now I know
               """

  // Do not use /dev/null on Windows. It leads to FileNotFoundException
  final val devNull = if (Platform.isWindows) "NUL" else "/dev/null"

  final val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)

  @Test def printStreamOutputStreamStringWithUnsupportedEncoding(): Unit = {
    assertThrows(
      classOf[java.io.UnsupportedEncodingException],
      new PrintStream(new File(devNull), "unsupported encoding")
    )
  }

  @Test def constructorString(): Unit = {
    val ps = new PrintStream(devNull)
    // check only that created ps is healthy enough so no Exception is thown
    ps.write(bytesUTF_8)
  }

  @Test def constructorStringString(): Unit = {
    val ps = new PrintStream(devNull, "UTF_16LE")

    // check only that created ps is healthy enough so no Exception is thown
    ps.write(bytesUTF_8)
  }
}

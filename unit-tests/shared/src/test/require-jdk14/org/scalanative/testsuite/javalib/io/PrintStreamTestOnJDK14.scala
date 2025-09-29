package org.scalanative.testsuite.javalib.io

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets
import java.util.Arrays

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class PrintStreamTestOnJDK14 {
  final val secondComing = """
               |""The best lack all conviction, while the worst
               |Are full of passionate intensity."
               """

  @Test def writeByteArray(): Unit = {
    val bytesUTF_16 = secondComing.getBytes(StandardCharsets.UTF_16)
    val bytesUTF_8 = secondComing.getBytes(StandardCharsets.UTF_8)
    val bytesExpected = bytesUTF_8

    val outStream = new ByteArrayOutputStream()

    val ps = new PrintStream(outStream, false, StandardCharsets.UTF_16)

    // The bytes should be written as given, not translated to UTF_16
    ps.writeBytes(bytesUTF_8)
    val bytesReceived = outStream.toByteArray()

    assertEquals(
      "size difference",
      bytesExpected.length,
      bytesReceived.length
    )

    val mmPos = Arrays.mismatch(bytesExpected, bytesReceived)
    assertEquals(
      s"expected($mmPos) != recieved(${mmPos})",
      -1,
      mmPos
    )
  }
}

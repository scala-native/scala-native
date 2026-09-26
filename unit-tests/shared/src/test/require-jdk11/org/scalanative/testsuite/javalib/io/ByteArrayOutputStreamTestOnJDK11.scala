package org.scalanative.testsuite.javalib.io

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.{util => ju}

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class ByteArrayOutputStreamTestOnJDK11 {

  private def byteArrayAsUTF_8(ba: Array[scala.Byte]): String =
    new String(ba, StandardCharsets.UTF_8)

  @Test def writeBytes_Exception(): Unit = {
    assertThrows(
      "Null argument",
      classOf[NullPointerException],
      (new ByteArrayOutputStream(1)).writeBytes(null)
    )
  }

  val testCases = ju.List.of(
    "",
    "Nobody ever figures out what life is all about" // R. Feynman
  )

  @Test def writeBytes_fromString(): Unit = {
    testCases.forEach(tc => {
      val bytes = tc.getBytes
      val outStream = new ByteArrayOutputStream()

      outStream.writeBytes(bytes)

      val outStreamBytes = outStream.toByteArray()

      val expected = byteArrayAsUTF_8(bytes)
      val got = byteArrayAsUTF_8(outStreamBytes)

      assertTrue(
        s"content differs:\n\texpected: |${expected}|\n\treceived: |${got}|\n",
        ju.Arrays.equals(bytes, outStreamBytes)
      )
    })
  }
}

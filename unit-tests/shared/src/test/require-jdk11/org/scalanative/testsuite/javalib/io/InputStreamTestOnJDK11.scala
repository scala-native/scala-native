package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamTestOnJDK11 {

  @Test def readNBytesLenNegativeLength(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)

    assertThrows(
      classOf[IllegalArgumentException],
      streamIn.readNBytes(-3)
    )
  }

  @Test def readNBytesLen(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val len = 5
    val result = streamIn.readNBytes(len)

    assertEquals("result length", len, result.length)
  }
}

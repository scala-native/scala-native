package org.scalanative.testsuite.javalib.io

import java.io._

import org.junit.Assert._
import org.junit.Test

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamTestOnJDK9 {

  @Test def readAllBytes(): Unit = {

    val inputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val result = streamIn.readAllBytes()

    assertEquals("result length", inputBytes.length, result.length)
  }

  @Test def readNBytesBufferOffLenExceptions(): Unit = {
    val inputBytes =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val receiver = new Array[Byte](10)
    val nRead = streamIn.readNBytes(receiver, 0, receiver.length)

    assertThrows(
      classOf[NullPointerException],
      streamIn.readNBytes(null, 0, receiver.length)
    )

    assertThrows(
      classOf[IndexOutOfBoundsException],
      streamIn.readNBytes(receiver, -2, receiver.length)
    )

    assertThrows(
      classOf[IndexOutOfBoundsException],
      streamIn.readNBytes(receiver, 0, -3)
    )

    assertThrows(
      classOf[IndexOutOfBoundsException],
      streamIn.readNBytes(receiver, 0, Integer.MAX_VALUE)
    )
  }

  @Test def readNBytesBufferOffLen(): Unit = {
    // Read all bytes in InputStream: exactly sized receiver.

    val inputBytes =
      List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).map(_.toByte).toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val receiver = new Array[Byte](10)
    val nRead = streamIn.readNBytes(receiver, 0, receiver.length)

    assertEquals("nRead", receiver.length, nRead)

    val expected = 9
    assertEquals("expected content", expected, receiver(expected))
  }

  @Test def readNBytesBufferOffNotAllInput(): Unit = {
    // Read fewer bytes than are available in InputStream: short receiver.

    val inputBytes = (0 until 12).map(_.toByte).toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)

    val receiverLength = inputBytes.length - 2
    val receiver = new Array[Byte](receiverLength)

    val nRead = streamIn.readNBytes(receiver, 0, receiverLength)

    assertEquals("nRead", receiverLength, nRead)

    val expected = 9
    assertEquals("expected content", expected, receiver(expected))
  }

  @Test def transferToNullArg(): Unit = {
    // Distinguish from test of Java 11 static method nullOutputStream().

    val inputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val streamOut = null.asInstanceOf[ByteArrayOutputStream]

    assertThrows(
      classOf[NullPointerException],
      streamIn.transferTo(streamOut)
    )
  }

  @Test def transferTo(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val streamOut = new ByteArrayOutputStream()

    val nTransferred = streamIn.transferTo(streamOut).toInt

    assertEquals("nBytes transferred", inputBytes.length, nTransferred)
    assertEquals("streamOut size", nTransferred, streamOut.size())

    val outputBytes = streamOut.toByteArray()
    for (j <- 0 until inputBytes.length)
      assertEquals(s"in(${j}) != out(${j})", inputBytes(j), outputBytes(j))
  }

}

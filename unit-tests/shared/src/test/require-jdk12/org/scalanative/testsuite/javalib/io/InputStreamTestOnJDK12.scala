package org.scalanative.testsuite.javalib.io

import java.io._
import java.util.Arrays

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamTestOnJDK12 {

  @Test def skipNBytesZeroN(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)

    streamIn.skipNBytes(0)
    val result = streamIn.readAllBytes()

    // Assert zero bytes were skipped
    assertEquals("result length", inputBytes.length, result.length)
  }

  @Test def skipNBytesNegativeN(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)

    streamIn.skipNBytes(-2)
    val result = streamIn.readAllBytes()

    // Assert zero bytes were skipped
    assertEquals("result length", inputBytes.length, result.length)
  }

  @Test def skipNBytesSmallN(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)

    // avoid starting skip at index 0 in case that index is special.
    val discardedLen = 1
    val discarded = streamIn.readNBytes(discardedLen)
    assertEquals("discarded length", discardedLen, discarded.length)

    val skipLen = 3
    streamIn.skipNBytes(skipLen)

    val remainingBytes = streamIn.readAllBytes()

    val expected = inputBytes.length - discardedLen - skipLen
    assertEquals("result length", expected, remainingBytes.length)
  }

  @Test def skipNBytesLargeN(): Unit = {
    /* Skip an arbitrarily "large" number of Bytes where this test
     * does not "know" if the implementation is buffered or not.
     */

    val shouldBeSkippedFragmentSize = 3
    val remainingFragmentSize = 5

    val nToSkip = 8192 + shouldBeSkippedFragmentSize

    val inputBytes = new Array[Byte](nToSkip + remainingFragmentSize)

    for (j <- 0 until remainingFragmentSize)
      inputBytes(nToSkip + j) = (j + 1).toByte

    val streamIn = new ByteArrayInputStream(inputBytes)

    // Start skipping at index 0 in order to exercise "no prior read()" case.
    streamIn.skipNBytes(nToSkip)

    val remainingBytes = streamIn.readAllBytes()

    assertEquals("result length", remainingFragmentSize, remainingBytes.length)

    val expectedBytes =
      Arrays.copyOfRange(inputBytes, nToSkip, inputBytes.length)

    assertArrayEquals("result content", expectedBytes, remainingBytes)
  }

}

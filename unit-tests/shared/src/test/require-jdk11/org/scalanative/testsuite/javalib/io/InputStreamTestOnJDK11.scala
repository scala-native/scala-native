package org.scalanative.testsuite.javalib.io

import java.io._
import java.util.Arrays

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamTestOnJDK11 {

  @Test def readNBytesOneArgNegativeLength(): Unit = {
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

  @Test def readNBytesOneArgSmallN(): Unit = {
    val inputBytes =
      List(255, 254, 253, 252, 251, 128, 127, 2, 1, 0)
        .map(_.toByte)
        .toArray[Byte]

    val streamIn = new ByteArrayInputStream(inputBytes)
    val len = 5
    val result = streamIn.readNBytes(len)

    assertEquals("result length", len, result.length)
    assertArrayEquals("result content", Arrays.copyOf(inputBytes, len), result)
  }

  @Test def readNBytesOneArgLargeN(): Unit = {
    /* Read more bytes than the presumably known implementation buffer size
     * but not the entire stream.
     */

    /* This "constant" must be manually kept in sync with corresponding
     * 'streamChunkSize' value in InputStream.scala.
     */
    val readNbChunkSize = 4096 // must match value in InputStream.scala

    val shouldBeReadFragmentSize = 5
    val shouldBeUnreadFragmentSize = 3

    // Force read of two full internal buffers, then a partial.
    val nToRead = (2 * readNbChunkSize) + shouldBeReadFragmentSize

    // For confusion, have some unread bytes at tail end of streamIn
    val nInputBytes = nToRead + shouldBeUnreadFragmentSize

    val inputBytes = new Array[Byte](nInputBytes)

    /* Place some non-zero data astride two internal chunks; some
     * at the end of one, some at the beginning of the following chunk.
     * Try to trip-up the buffering logic and expose any bugs.
     */

    val lookback = 2
    val startValidData = nToRead - shouldBeReadFragmentSize - lookback

    for (j <- startValidData until nToRead)
      inputBytes(j) = j.toByte

    val streamIn = new ByteArrayInputStream(inputBytes)

    val result = streamIn.readNBytes(nToRead)

    assertEquals("result length", nToRead, result.length)

    assertArrayEquals(
      "result content",
      Arrays.copyOfRange(inputBytes, startValidData, nToRead),
      Arrays.copyOfRange(result, startValidData, nToRead)
    )
  }

}

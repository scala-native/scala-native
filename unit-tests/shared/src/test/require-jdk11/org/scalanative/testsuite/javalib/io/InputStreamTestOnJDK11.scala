package org.scalanative.testsuite.javalib.io

import java.io.*
import java.util.Arrays

import org.junit.Test
import org.junit.Assert.*

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

  @Test def nullInputStreamWhenOpen(): Unit = {
    val streamIn = InputStream.nullInputStream()
    val streamOut = new ByteArrayOutputStream()

    val buffer = new Array[Byte](8)

    assertEquals("available()", 0, streamIn.available())

    assertEquals("markSupported()", false, streamIn.markSupported())

    streamIn.mark(1) // should do nothing.

    assertEquals("read()", -1, streamIn.read())

    assertEquals("read(buffer)", -1, streamIn.read(buffer))

    assertEquals(
      "read(buffer, off, len)",
      -1,
      streamIn.read(buffer, 1, buffer.length - 2)
    )

    assertEquals("readAllBytes()", 0, streamIn.readAllBytes().length)

    assertEquals(
      "readNBytes(buffer, off, len)",
      0,
      streamIn.readNBytes(buffer, 2, buffer.length - 3)
    )

    assertEquals(
      "readNBytes(len)",
      0,
      streamIn.readNBytes(buffer.length - 3).length
    )

    assertThrows(
      "reset()",
      classOf[IOException],
      streamIn.reset()
    )

    assertEquals("skip(len)", 0L, streamIn.skip(buffer.length - 3))

    assertEquals("transferTo(streamOut)", 0L, streamIn.transferTo(streamOut))
    assertEquals("streamOut.length)", 0L, streamOut.size())

    streamIn.close() // close of open stream should succeed
  }

  @Test def nullInputStreamWhenClosed(): Unit = {
    val streamIn = InputStream.nullInputStream()
    val streamOut = new ByteArrayOutputStream()

    val buffer = new Array[Byte](8)

    streamIn.close()

    assertThrows(
      "available()",
      classOf[IOException],
      streamIn.available()
    )

    assertEquals("markSupported()", false, streamIn.markSupported())

    streamIn.mark(1) // should do nothing.

    assertThrows(
      "read()",
      classOf[IOException],
      streamIn.read()
    )

    assertThrows(
      "read(buffer)",
      classOf[IOException],
      streamIn.read(buffer)
    )

    assertThrows(
      "read(buffer, off, len)",
      classOf[IOException],
      streamIn.read(buffer, 1, buffer.length - 2)
    )

    assertThrows(
      "readAllBytes()",
      classOf[IOException],
      streamIn.readAllBytes()
    )

    assertThrows(
      "readNBytes(buffer, off, len)",
      classOf[IOException],
      streamIn.readNBytes(buffer, 2, buffer.length - 3)
    )

    assertThrows(
      "readNBytes(len)",
      classOf[IOException],
      streamIn.readNBytes(buffer.length - 3).length
    )

    // reset() has same behavior, open or closed.
    assertThrows(
      "reset()",
      classOf[IOException],
      streamIn.reset()
    )

    assertThrows(
      "skip(len)",
      classOf[IOException],
      streamIn.skip(buffer.length - 3)
    )

    assertThrows(
      "transferTo(streamOut)",
      classOf[IOException],
      streamIn.transferTo(streamOut)
    )

    streamIn.close() // close of closed stream should succeed
  }

}

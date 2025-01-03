package org.scalanative.testsuite.javalib.io

import java.io._
import java.util.Arrays

import org.junit.Test
import org.junit.Assert._

import org.scalanative.testsuite.utils.AssertThrows.assertThrows

class InputStreamTest {

  class MockInputStreamForSkipping(buf: Array[Byte]) extends InputStream {
    var pos = 0
    val count = buf.length

    def read(): Int = {
      if (pos >= count) -1
      else {
        val index = pos
        pos += 1
        buf(index).toByte
      }
    }

    /* To reduce CI time, use a clone of ByteArrayInputStream code for bulk
     * read(b, off, len) instead of the single character read() based
     * implementation in the InputStream base class.
     */

    override def read(b: Array[Byte], off: Int, reqLen: Int): Int = {
      if (off < 0 || reqLen < 0 || reqLen > b.length - off)
        throw new IndexOutOfBoundsException

      val len = Math.min(reqLen, count - pos)

      if (reqLen == 0)
        0 // 0 requested, 0 returned
      else if (len == 0)
        -1 // nothing to read at all
      else {
        System.arraycopy(buf, pos, b, off, len)
        pos += len
        len
      }
    }
  }

  @Test def skipLargeN(): Unit = {

    val shouldBeSkippedFragmentSize = 3
    val remainingFragmentSize = 5

    val nToSkip = 8192 + shouldBeSkippedFragmentSize

    val inputBytes = new Array[Byte](nToSkip + remainingFragmentSize)

    for (j <- 0 until remainingFragmentSize)
      inputBytes(nToSkip + j) = (j + 1).toByte

    // Ensure skip() implementation of base class is used, not an override
    val streamIn = new MockInputStreamForSkipping(inputBytes)

    // Start skipping at index 0 in order to exercise "no prior read()" case.
    val nActuallySkipped = streamIn.skip(nToSkip)
    assertEquals("n skipped", nToSkip, nActuallySkipped)

    val remainingBytes = new Array[Byte](remainingFragmentSize)

    val nRemainingBytes = streamIn.read(remainingBytes)

    assertEquals("result length", remainingFragmentSize, nRemainingBytes)

    val expectedBytes =
      Arrays.copyOfRange(inputBytes, nToSkip, inputBytes.length)

    assertArrayEquals("result content", expectedBytes, remainingBytes)
  }

}

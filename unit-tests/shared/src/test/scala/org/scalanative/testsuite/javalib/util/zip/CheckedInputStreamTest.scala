package org.scalanative.testsuite.javalib.util.zip

// Ported from Apache Harmony

import java.util.zip.*
import java.io.ByteArrayInputStream

import org.junit.Test
import org.junit.Assert.*

class CheckedInputStreamTest {

  @Test def constructor(): Unit = {
    val checkIn =
      new CheckedInputStream(new ByteArrayInputStream(checkInput), new CRC32())
    assertTrue(checkIn.getChecksum().getValue() == 0)
  }

  @Test def getChecksum(): Unit = {
    val outBuf = new Array[Byte](100)
    val emptyIn = new ByteArrayInputStream(Array.empty[Byte])
    val checkEmpty = new CheckedInputStream(emptyIn, new CRC32())
    while (checkEmpty.read() >= 0) {}
    assertTrue(checkEmpty.getChecksum().getValue() == 0)
    emptyIn.close()

    val checkIn =
      new CheckedInputStream(new ByteArrayInputStream(checkInput), new CRC32())
    while (checkIn.read() >= 0) {}
    assertTrue(checkIn.getChecksum().getValue() == 2036203193)
    checkIn.close()

    val checkIn2 =
      new CheckedInputStream(new ByteArrayInputStream(checkInput), new CRC32())
    checkIn2.read(outBuf, 0, 10)
    assertTrue(checkIn2.getChecksum().getValue() == 2235765342L)
    checkIn2.close()
  }

  @Test def skipJ(): Unit = {
    val checkIn =
      new CheckedInputStream(new ByteArrayInputStream(checkInput), new CRC32())
    val skipValue = 5
    assertTrue(checkIn.skip(skipValue) == skipValue)
    assertTrue(checkIn.getChecksum().getValue() == 668091412L)
    assertTrue(checkIn.skip(0) == 0)
    checkIn.close()
  }

  val checkInput =
    Array[Byte](9, 99, 114, 99, 46, 114, 101, 115, 101, 116, 40, 41, 59, 13, 10,
      9, 99, 114, 99, 46, 117, 112, 100, 97, 116, 101, 40, 49, 41, 59, 13, 10,
      9, 47, 47, 83, 121, 115, 116, 101, 109, 46, 111, 117, 116, 46, 112, 114,
      105, 110, 116, 40, 34, 118, 97, 108, 117, 101, 32, 111, 102, 32, 99, 114,
      99, 34, 43, 99, 114, 99, 46, 103, 101, 116, 86, 97, 108, 117, 101, 40, 41,
      41, 59, 32, 13, 10, 9)

}
